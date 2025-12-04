package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.Comment;
import com.acgforum.acgbackend.entity.Notification;
import com.acgforum.acgbackend.entity.Topic;
import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.repository.CommentRepository;
import com.acgforum.acgbackend.repository.NotificationRepository;
import com.acgforum.acgbackend.repository.TopicRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired private NotificationRepository notificationRepository; // 【新增】
    // 注入配置里的上传路径 (如果之前没写，记得加上)
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 1. 发表评论 (支持图片)
    @PostMapping("/add")
    public Map<String, Object> addComment(
            @RequestParam("topicId") Long topicId,
            @RequestParam("content") String content,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "file", required = false) MultipartFile file, // 【新增】接收文件
            HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();

        // 1. 检查登录
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }

        if (content == null || content.trim().isEmpty()) {
            result.put("success", false);
            result.put("msg", "内容不能为空");
            return result;
        }

        // 2. 查找 Topic
        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic == null) {
            result.put("success", false);
            result.put("msg", "话题不存在");
            return result;
        }

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setTopic(topic);

        // 3. 处理父评论
        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId).orElse(null);
            comment.setParent(parentComment);
        }

        // 4. 【新增】处理图片上传
        if (file != null && !file.isEmpty()) {
            try {
                // 确保目录存在
                java.io.File directory = new java.io.File(uploadDir);
                if (!directory.exists()) directory.mkdirs();

                // 生成文件名
                String originalFilename = file.getOriginalFilename();
                String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = java.util.UUID.randomUUID().toString() + ext;

                // 保存
                java.io.File dest = new java.io.File(directory.getAbsolutePath() + java.io.File.separator + newFilename);
                file.transferTo(dest);

                // 设置路径
                comment.setImageUrl("/files/" + newFilename);

            } catch (java.io.IOException e) {
                e.printStackTrace();
                // 图片上传失败可以只记录日志，不阻断评论发布
            }
        }
        
        commentRepository.save(comment);



        // =========== 👇【新增】通知逻辑开始 👇 ===========
        
        // 目标用户 (我们要通知谁？)
        User targetUser = null;
        String msgContent = "";

        if (parentId != null) {
            // 情况 A：这是楼中楼 -> 通知父评论的作者
            Comment parent = comment.getParent();
            targetUser = parent.getUser();
            msgContent = "replied to your comment";
        } else {
            // 情况 B：这是直接评论帖子 -> 通知帖主
            targetUser = topic.getAuthor();
            msgContent = "replied to your post: " + topic.getTitle();
        }

        // 关键判断：自己回复自己不用通知
        if (targetUser != null && !targetUser.getId().equals(user.getId())) {
            Notification notify = new Notification();
            notify.setReceiver(targetUser);
            notify.setActor(user); // 触发者是当前登录用户
            notify.setTopic(topic);
            notify.setMessage(msgContent);
            notify.setRead(false);
            notificationRepository.save(notify);
        }
        // =========== 👆【新增】通知逻辑结束 👆 ===========

        result.put("success", true);
        result.put("msg", "评论成功");
        return result;
    }
    
    // 2. 获取某个话题的评论列表
    @GetMapping("/list")
    public List<Comment> getComments(@RequestParam Long topicId) {
        return commentRepository.findByTopicIdOrderByCreatedAtDesc(topicId);
    }
    // 【新增】删除评论 (逻辑删除)
    @PostMapping("/delete")
    public Map<String, Object> deleteComment(@RequestBody Map<String, Long> payload, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }

        Long commentId = payload.get("commentId");
        Comment comment = commentRepository.findById(commentId).orElse(null);

        if (comment != null) {
            // 权限检查
            if (comment.getUser() != null && comment.getUser().getId().equals(user.getId())) {
                // 逻辑删除：抹除内容和作者，但保留记录占位
                comment.setContent("[该评论已删除]");
                comment.setUser(null); // 作者变空
                comment.setImageUrl(null); // 图片清空
                commentRepository.save(comment);
                
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("msg", "无权删除");
            }
        } else {
            result.put("success", false);
            result.put("msg", "评论不存在");
        }
        
        return result;
    }
}
package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.Comment;
import com.acgforum.acgbackend.entity.Topic;
import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.repository.CommentRepository;
import com.acgforum.acgbackend.repository.TopicRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // 1. 发表评论
    @PostMapping("/add")
    public Map<String, Object> addComment(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 检查登录
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }

        // 获取参数 (topicId 和 content)
        String content = (String) payload.get("content");
        Long topicId = Long.valueOf(payload.get("topicId").toString());

        if (content == null || content.trim().isEmpty()) {
            result.put("success", false);
            result.put("msg", "评论内容不能为空");
            return result;
        }

        // 查找对应的话题
        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic == null) {
            result.put("success", false);
            result.put("msg", "话题不存在");
            return result;
        }

        // 保存评论
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setTopic(topic);
        
        commentRepository.save(comment);

        result.put("success", true);
        result.put("msg", "评论成功");
        return result;
    }

    // 2. 获取某个话题的评论列表
    @GetMapping("/list")
    public List<Comment> getComments(@RequestParam Long topicId) {
        return commentRepository.findByTopicIdOrderByCreatedAtDesc(topicId);
    }
}
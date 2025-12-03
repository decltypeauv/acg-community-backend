package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.Media;
import com.acgforum.acgbackend.entity.Topic;
import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.repository.MediaRepository;
import com.acgforum.acgbackend.repository.TopicRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/topic")
public class TopicController {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private MediaRepository mediaRepository;

    // 读取配置文件里的上传路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 【核心升级】发布话题（支持带附件）
    // 注意：这里不用 @RequestBody，因为要接收文件流，直接用参数接收
    @PostMapping("/publish")
    public Map<String, Object> publish(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "category", defaultValue = "General") String category, // <--- 新增这行
            @RequestParam(value = "files", required = false) MultipartFile[] files, // 允许不传图，也可以传多个
            HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();

        // 1. 检查登录
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }

        // 2. 保存帖子本体
        Topic topic = new Topic();
        topic.setTitle(title);
        topic.setContent(content);
        topic.setCategory(category); // <--- 新增这行：保存分类
        topic.setAuthor(user);
        Topic savedTopic = topicRepository.save(topic); // 先保存，拿到 ID

        // 3. 处理附件 (如果有)
        if (files != null && files.length > 0) {
            File directory = new File(uploadDir);
            if (!directory.exists()) directory.mkdirs();

            List<Media> mediaList = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                try {
                    // 生成文件名
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String newFilename = UUID.randomUUID().toString() + extension;

                    // 存硬盘
                    File dest = new File(directory.getAbsolutePath() + File.separator + newFilename);
                    file.transferTo(dest);

                    // 存数据库
                    Media media = new Media();
                    media.setTitle("附件"); // 暂时叫附件，也可以前端传
                    media.setFilename(newFilename);
                    media.setUrl("/files/" + newFilename);
                    // 判断类型
                    String type = extension.toLowerCase().matches(".*(mp4|avi|mov)$") ? "VIDEO" : "IMAGE";
                    media.setType(type);
                    
                    media.setUploader(user);
                    media.setTopic(savedTopic); // 【关键】关联到刚才创建的帖子

                    mediaRepository.save(media);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    // 这里可以记录日志，暂不打断
                }
            }
        }

        result.put("success", true);
        result.put("msg", "发布成功");
        return result;
    }

    // 获取列表 (Entity 修改后，这里自动会带出 mediaList)
   // 获取列表 (支持：分类筛选、搜索、排序)
    @GetMapping("/list")
    public List<Topic> getTopicList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "New") String sort // New (最新) 或 Top (高分)
    ) {
        List<Topic> list;

        // 1. 先判断是不是搜索 (搜索优先级最高)
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = topicRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(keyword);
        } 
        // 2. 再判断是不是按分类查
        else if (category != null && !category.isEmpty() && !category.equals("All")) {
            // 如果是分类，还要看是按时间(New)还是按票数(Top)
            if ("Top".equals(sort)) {
                list = topicRepository.findByCategoryOrderByVoteCountDesc(category);
            } else {
                list = topicRepository.findByCategoryOrderByCreatedAtDesc(category);
            }
        } 
        // 3. 查所有
        else {
            if ("Top".equals(sort)) {
                list = topicRepository.findAllByOrderByVoteCountDesc();
            } else {
                list = topicRepository.findAllByOrderByCreatedAtDesc();
            }
        }
        
        return list;
    }
    
    // 获取详情
    @GetMapping("/{id}")
    public Topic getTopicDetail(@PathVariable Long id) {
        return topicRepository.findById(id).orElse(null);
    }

    // 👇 必须要有这个接口，前端才能获取发帖记录 👇
    @GetMapping("/user/{userId}")
    public List<Topic> getUserTopics(@PathVariable Long userId) {
        return topicRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    // 【新增】删除话题
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteTopic(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }

        Topic topic = topicRepository.findById(id).orElse(null);
        if (topic == null) {
            result.put("success", false);
            result.put("msg", "帖子不存在");
            return result;
        }

        // 权限检查：
        // 1. 帖子必须有作者 (老数据可能没有)
        // 2. 作者 ID 必须和当前登录用户一致
        if (topic.getAuthor() == null || !topic.getAuthor().getId().equals(user.getId())) {
            result.put("success", false);
            result.put("msg", "无权删除 (或该帖无归属)");
            return result;
        }

        // 删除 (JPA 会自动级联删除关联的 Media 和 Comment)
        topicRepository.delete(topic);

        result.put("success", true);
        result.put("msg", "删除成功");
        return result;
    }
}
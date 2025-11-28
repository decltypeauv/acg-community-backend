package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.Media;
import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.repository.MediaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private MediaRepository mediaRepository;

    // 读取配置文件里的上传路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 1. 上传接口
    @PostMapping("/upload")
    public Map<String, Object> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();

        // 检查登录状态
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("msg", "请先登录！");
            return result;
        }

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("msg", "文件不能为空");
            return result;
        }

        try {
            // 1. 确保目录存在
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // 自动创建 uploads 文件夹
            }

            // 2. 生成唯一文件名 (防止重名覆盖)
            // 例如：original.jpg -> uuid-uuid.jpg
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 3. 保存文件到硬盘
            File dest = new File(directory.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(dest);

            // 4. 判断类型 (简单判断)
            String type = "IMAGE";
            if (extension.toLowerCase().matches(".*(mp4|avi|mov)$")) {
                type = "VIDEO";
            }

            // 5. 保存到数据库
            Media media = new Media();
            media.setTitle(title);
            media.setFilename(newFilename);
            media.setUrl("/files/" + newFilename); // 前端访问的路径
            media.setType(type);
            media.setUploader(user); // 记录是谁传的
            
            mediaRepository.save(media);

            result.put("success", true);
            result.put("msg", "上传成功");
            result.put("url", media.getUrl());

        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("msg", "上传失败：" + e.getMessage());
        }

        return result;
    }

    // 2. 获取所有媒体列表 (用于首页展示)
    @GetMapping("/list")
    public List<Media> getAllMedia() {
        return mediaRepository.findAllByOrderByUploadTimeDesc();
    }
}
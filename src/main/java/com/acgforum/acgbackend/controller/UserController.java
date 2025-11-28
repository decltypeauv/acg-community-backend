package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 更新头像接口
    @PostMapping("/update-avatar")
    public Map<String, Object> updateAvatar(@RequestParam("file") MultipartFile file, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 1. 检查登录
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("msg", "文件为空");
            return result;
        }

        try {
            // 2. 保存图片到硬盘 (复用之前的逻辑)
            File directory = new File(uploadDir);
            if (!directory.exists()) directory.mkdirs();

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            File dest = new File(directory.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(dest);

            // 3. 更新用户数据库
            String avatarUrl = "/files/" + newFilename;
            
            // 重新从数据库查一次用户，确保数据最新
            User userInDb = userRepository.findById(currentUser.getId()).orElse(null);
            if(userInDb != null) {
                userInDb.setAvatar(avatarUrl);
                userRepository.save(userInDb);
                
                // 更新 Session 里的用户信息
                session.setAttribute("user", userInDb);
            }

            result.put("success", true);
            result.put("msg", "头像更新成功");
            result.put("url", avatarUrl);

        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("msg", "上传失败");
        }

        return result;
    }
}
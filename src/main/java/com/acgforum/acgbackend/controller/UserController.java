package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.mindrot.jbcrypt.BCrypt;
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
    // 【新增】获取公开用户信息 (头像、昵称、注册时间)
    @GetMapping("/{id}")
    public User getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(null); // 🔐 安全第一：千万别把密码返回给前端！
        }
        return user;
    }

    // 1. 修改基本资料 (昵称)
    @PostMapping("/update-info")
    public Map<String, Object> updateInfo(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }

        String newNickname = data.get("nickname");
        if (newNickname != null && !newNickname.trim().isEmpty()) {
            // 重新从数据库获取最新对象，防止 session 数据滞后
            User userInDb = userRepository.findById(currentUser.getId()).orElse(null);
            if (userInDb != null) {
                userInDb.setNickname(newNickname);
                userRepository.save(userInDb);
                session.setAttribute("user", userInDb); // 更新 session
                result.put("success", true);
                result.put("msg", "昵称已更新");
            }
        } else {
            result.put("success", false);
            result.put("msg", "昵称不能为空");
        }
        return result;
    }

    //修改密码
   @PostMapping("/update-password")
    public Map<String, Object> updatePassword(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("user"); // 这里的 user 是 session 里的，密码可能是旧的

        if (currentUser == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }

        String oldPass = data.get("oldPassword");
        String newPass = data.get("newPassword");

        // 从数据库查出最新的用户信息（包含加密后的密码）
        User userInDb = userRepository.findById(currentUser.getId()).orElse(null);
        
        if (userInDb != null) {
            // 1. 验证旧密码 (用 checkpw)
            if (BCrypt.checkpw(oldPass, userInDb.getPassword())) {
                
                // 2. 加密新密码
                String hashedNewPass = BCrypt.hashpw(newPass, BCrypt.gensalt());
                userInDb.setPassword(hashedNewPass);
                
                userRepository.save(userInDb);
                result.put("success", true);
                result.put("msg", "密码修改成功");
            } else {
                result.put("success", false);
                result.put("msg", "旧密码错误");
            }
        } else {
            result.put("success", false);
            result.put("msg", "用户异常");
        }
        return result;
    }
}
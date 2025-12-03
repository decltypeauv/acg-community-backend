package com.acgforum.acgbackend.service;

import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 注册逻辑
    public String register(User user) {
        // 1. 检查是否重名
        if (userRepository.existsByUsername(user.getUsername())) {
            return "用户名已存在！";
        }

        // 2. 设置默认值
        // 如果没填昵称，默认和账号一样
        if (user.getNickname() == null || user.getNickname().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        // 设置默认头像 (可以是网上的图，也可以是你本地 uploads 里的默认图)
        if (user.getAvatar() == null) {
            user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getUsername()); 
            // 这里用了一个开源头像生成API，这样每个新用户的默认头像都不一样！
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }

        // 【新增】密码加密的核心代码
        // BCrypt.gensalt() 会自动生成随机盐，混入密码中
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword); // 把加密后的乱码塞回去
        // 3. 保存 (实际项目中记得加密密码，这里演示存明文)
        userRepository.save(user);
        return "注册成功";
    }

    // 登录逻辑 (保持不变)
    public User login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username).orElse(null);

        // 【修改】使用 checkpw 验证密码
        // 参数1：用户输入的明文 (123)
        // 参数2：数据库里的密文 ($2a$10$...)
        if (BCrypt.checkpw(rawPassword, user.getPassword())) {
            return user;
        }
        return null;
    }
}
package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // 允许跨域，方便前端调试
public class AuthController {

    @Autowired
    private UserService userService;

    // 注册接口
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        String result = userService.register(user);
        Map<String, Object> map = new HashMap<>();
        map.put("msg", result);
        map.put("success", "注册成功".equals(result));
        return map;
    }

    // 登录接口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        User user = userService.login(username, password);
        Map<String, Object> map = new HashMap<>();

        if (user != null) {
            // 登录成功，将用户信息存入 Session，实现“解锁功能”
            session.setAttribute("user", user);
            
            map.put("success", true);
            map.put("msg", "欢迎回来，" + user.getNickname());
            map.put("user", user); // 返回用户信息给前端保存
        } else {
            map.put("success", false);
            map.put("msg", "账号或密码错误");
        }
        return map;
    }
    
    // 检查登录状态（用于判断是否解锁下载按钮）
    @GetMapping("/check")
    public Map<String, Object> checkLogin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> map = new HashMap<>();
        if (user != null) {
            map.put("isLogin", true);
            map.put("user", user);
        } else {
            map.put("isLogin", false);
        }
        return map;
    }
    
    // 退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "已退出";
    }
}
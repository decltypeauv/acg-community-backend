package com.acgforum.acgbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home_1")
    public String home_1(Model model) {
        // 可以添加数据到模型中，在HTML页面中使用
        model.addAttribute("message", "欢迎来到萌萌乐园！");
        model.addAttribute("title", "我的Spring Boot网站");
        return "home_1"; // 这对应 templates/shouye1.html
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/homne_1"; // 访问根路径时重定向到shouye1
    }
}
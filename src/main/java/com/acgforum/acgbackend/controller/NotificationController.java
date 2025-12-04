package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.Notification;
import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // 1. 获取未读数量 (用于 header 红点)
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> res = new HashMap<>();
        if (user != null) {
            long count = notificationRepository.countByReceiverIdAndIsReadFalse(user.getId());
            res.put("count", count);
        } else {
            res.put("count", 0);
        }
        return res;
    }

    // 2. 获取我的通知列表
    @GetMapping("/list")
    public List<Notification> getMyNotifications(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return null;
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(user.getId());
    }

    // 3. 标记为已读 (当用户点击某条通知时调用)
    @PostMapping("/read/{id}")
    public void markAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
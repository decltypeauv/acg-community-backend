package com.acgforum.acgbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 谁接收通知？
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    // 谁触发的？(比如谁回复了你)
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    // 关联哪个帖子？(方便跳转)
    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    // 通知内容 (例如: "回复了你的帖子", "回复了你的评论")
    private String message;

    // 是否已读
    private boolean isRead = false;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
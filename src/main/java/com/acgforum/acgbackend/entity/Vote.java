package com.acgforum.acgbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "topic_id"}) // 确保一个用户对一个帖子只能有一条记录
})
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 谁投的
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 投给谁
    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    // 投的什么：1 (Upvote), -1 (Downvote)
    private int type; 
}
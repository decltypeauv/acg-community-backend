package com.acgforum.acgbackend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "comment_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "comment_id"}) // 每个人对每条评论只能投一次
})
public class CommentVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    private int type; // 1 (赞), -1 (踩)
}
package com.acgforum.acgbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 
import java.util.List; 

@Data
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // 评论内容

    // 关联评论人
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 关联话题 (这是关键，评论必须属于某个话题)
    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    private LocalDateTime createdAt;
    // 【新增】评论附带的图片路径
    private String imageUrl;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
     // 【新增】我是回复哪条评论的？(父评论)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"replies", "topic", "user"}) // 防止 JSON 死循环
    private Comment parent;

    // 【新增】我有多少条子回复？
    // mappedBy="parent" 表示由上面那个 parent 字段来维护关系
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"parent", "topic"}) // 同样防止死循环
    private List<Comment> replies;

    // 【新增】评论的点赞数
    private Integer voteCount = 0;

    // 【新增】级联删除：删评论时，自动把这条评论收到的“赞/踩”记录全删掉
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("comment") // 防止 JSON 死循环
    private List<CommentVote> commentVotes;
}
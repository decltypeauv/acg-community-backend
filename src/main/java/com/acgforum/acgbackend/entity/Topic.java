package com.acgforum.acgbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Data
@Entity
@Table(name = "topics")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;   // 话题标题

    @Column(columnDefinition = "TEXT") 
    private String content; // 话题内容 (用 TEXT 类型，因为内容可能很长)

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;    // 发帖人

    private LocalDateTime createdAt;
     // 【新增】记录当前帖子的总分 (点赞+1，点踩-1)
    private Integer voteCount = 0;
    // 【新增】分类字段 (例如: "Anime", "Game", "Art")
    private String category;
    // 这里可以加一个字段统计回复数，暂时先不加，简单点
    
    // 【新增】一个话题包含多个媒体文件
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("topic") // 防止 JSON 死循环：查帖子时显示媒体，但媒体里别再套帖子
    private List<Media> mediaList;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
    // 【新增】级联删除：删帖子时，自动删掉下面的所有评论
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"topic", "parent", "replies"}) // 防止死循环
    private List<Comment> comments;


    
    // 【新增】级联删除：删帖子时，自动删掉相关的点赞记录
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("topic") // 防止死循环
    private List<Vote> votes;
}
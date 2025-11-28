package com.acgforum.acgbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 

@Data
@Entity
@Table(name = "media")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;       // 标题 (用户填写的)
    private String filename;    // 存储在硬盘上的文件名 (例如 uuid.jpg)
    private String url;         // 访问链接 (例如 /files/uuid.jpg)
    private String type;        // 类型: IMAGE 或 VIDEO

    // 关联上传者 (多对一：一个用户可以传很多图)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User uploader;

    private LocalDateTime uploadTime;

    // 【新增】关联到话题
    @ManyToOne
    @JoinColumn(name = "topic_id")
    @JsonIgnoreProperties("mediaList") // 防止 JSON 死循环：查图时不显示帖子详情
    private Topic topic;

    
    @PrePersist
    public void onCreate() {
        uploadTime = LocalDateTime.now();
    }
}
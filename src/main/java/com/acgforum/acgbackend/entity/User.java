package com.acgforum.acgbackend.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Lombok 自动生成 Getter/Setter
@Entity
@Table(name = "users")
public class User {  // 检查这一行，必须有 class
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nickname;
    private String email;
    private String avatar;
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (role == null) role = "USER";
        if (avatar == null) avatar = "https://example.com/default_miku.png";
    }
}
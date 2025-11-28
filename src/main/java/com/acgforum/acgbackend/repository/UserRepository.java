package com.acgforum.acgbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acgforum.acgbackend.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 通过用户名查找用户
    Optional<User> findByUsername(String username);
    // 检查用户名是否存在
    boolean existsByUsername(String username);
}
package com.acgforum.acgbackend.repository;

import com.acgforum.acgbackend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 根据话题ID查找评论，并按时间倒序排列
    List<Comment> findByTopicIdOrderByCreatedAtDesc(Long topicId);
}
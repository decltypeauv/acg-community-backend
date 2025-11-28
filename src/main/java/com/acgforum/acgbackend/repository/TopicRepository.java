package com.acgforum.acgbackend.repository;

import com.acgforum.acgbackend.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    // 获取所有话题，按时间倒序（最新的在最上面）
    List<Topic> findAllByOrderByCreatedAtDesc();
}
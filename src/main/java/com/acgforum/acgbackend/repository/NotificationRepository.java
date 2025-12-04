package com.acgforum.acgbackend.repository;
import org.springframework.data.jpa.repository.Modifying; 
import org.springframework.transaction.annotation.Transactional; 
import com.acgforum.acgbackend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 查某人的所有通知，按时间倒序
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    
    // 统计某人的未读数量
    long countByReceiverIdAndIsReadFalse(Long receiverId);

     // 【新增】根据帖子ID删除所有通知
    @Modifying
    @Transactional // 删除操作必须加事务注解
    void deleteByTopicId(Long topicId);
}
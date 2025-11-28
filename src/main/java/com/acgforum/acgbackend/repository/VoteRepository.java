package com.acgforum.acgbackend.repository;

import com.acgforum.acgbackend.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    // 查一下：这个人对这个帖子投过票吗？
    Optional<Vote> findByUserIdAndTopicId(Long userId, Long topicId);
}
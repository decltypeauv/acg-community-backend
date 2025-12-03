package com.acgforum.acgbackend.repository;

import com.acgforum.acgbackend.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    // 查一下：这个人对这个帖子投过票吗？
    Optional<Vote> findByUserIdAndTopicId(Long userId, Long topicId);
    // 【新增】查找某用户最近的 N 次点赞记录 (按时间倒序)
    // 我们只需要 type=1 (点赞) 的记录
    List<Vote> findByUserIdAndTypeOrderByIdDesc(Long userId, int type);
}
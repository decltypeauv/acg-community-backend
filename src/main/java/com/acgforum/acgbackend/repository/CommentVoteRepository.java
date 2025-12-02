package com.acgforum.acgbackend.repository;

import com.acgforum.acgbackend.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByUserIdAndCommentId(Long userId, Long commentId);
}
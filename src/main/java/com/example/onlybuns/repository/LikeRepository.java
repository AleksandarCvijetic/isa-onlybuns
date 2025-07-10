package com.example.onlybuns.repository;

import com.example.onlybuns.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    // You can add custom queries here if needed
    Like findByPostIdAndUserId(Long postId, Long userId);
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id IN ?1 AND l.likeDate > ?2")
    long countTotalNewLikesForPostsSinceLastLogin(List<Long> postIds, LocalDateTime lastLogin);
}

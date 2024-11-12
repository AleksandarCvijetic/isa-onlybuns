package com.example.onlybuns.repository;

import com.example.onlybuns.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    // You can add custom queries here if needed
    Like findByPostIdAndUserId(Long postId, Long userId);
}

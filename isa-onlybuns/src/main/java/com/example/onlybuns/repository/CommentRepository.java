package com.example.onlybuns.repository;

import com.example.onlybuns.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // You can add custom queries here if needed
}

package com.example.onlybuns.repository;

import com.example.onlybuns.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // You can add custom queries here if needed
    public List<Comment> getByPostId(Long postId);

}

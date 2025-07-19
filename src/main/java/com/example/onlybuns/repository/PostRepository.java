package com.example.onlybuns.repository;

import com.example.onlybuns.model.Post;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCreatedAtBefore(ZonedDateTime createdAt);
    List<Post> findByUser_Id(Long userId);
    List<Post> findTop10ByOrderByLikeCountDesc();
    List<Post> findByCreatedAtAfterOrderByLikeCountDesc(ZonedDateTime after);
}

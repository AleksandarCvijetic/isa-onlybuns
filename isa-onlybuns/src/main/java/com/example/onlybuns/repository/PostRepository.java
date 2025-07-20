package com.example.onlybuns.repository;

import com.example.onlybuns.model.Post;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCreatedAtBefore(ZonedDateTime createdAt);
    List<Post> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Post findByIdForUpdate(@Param("id") Long id);

    List<Post> getByUserIdIn(List<Long> followeeIds);
}

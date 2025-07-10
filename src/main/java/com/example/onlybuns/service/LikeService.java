package com.example.onlybuns.service;

import com.example.onlybuns.model.Like;
import com.example.onlybuns.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LikeService {

    private final LikeRepository likeRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public Like createLike(Like like) {

        like.setLikeDate(LocalDateTime.now());

        return likeRepository.save(like); // Save the like record in the database
    }

    public Like getLikeByPostAndUser(Long postId, Long userId) {
        return likeRepository.findByPostIdAndUserId(postId,userId);
    }
}

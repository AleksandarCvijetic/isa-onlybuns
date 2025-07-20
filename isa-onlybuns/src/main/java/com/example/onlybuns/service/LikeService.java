package com.example.onlybuns.service;

import com.example.onlybuns.model.Like;
import com.example.onlybuns.repository.LikeRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.onlybuns.model.Post;
import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LikeService {

    private final LikeRepository likeRepository;

    @Autowired
    public PostRepository postRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    public Like getLikeByPostAndUser(Long postId, Long userId) {
        return likeRepository.findByPostIdAndUserId(postId,userId);
    }

    public List<Like> getAllLikes(){
        return likeRepository.findAll();
    }
    @Transactional
    public Like createLike(UserInfo user, Long postId) {

        Post post = postRepository.findByIdForUpdate(postId);

        post.setLikeCount(post.getLikeCount() + 1);

        Like like = new Like();
        like.setLikeDate(LocalDateTime.now());

        like.setUser(user);
        like.getUser().setId(user.getId());
        like.setPost(post);

        postRepository.save(post);
        return likeRepository.save(like);
    }
}

package com.example.onlybuns.service;

import com.example.onlybuns.dtos.CommentCreationDto;
import com.example.onlybuns.dtos.CommentReadDto;
import com.example.onlybuns.model.Comment;
import com.example.onlybuns.model.Followers;
import com.example.onlybuns.model.Post;
import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.CommentRepository;
import com.example.onlybuns.repository.FollowersRepository;
import com.example.onlybuns.repository.PostRepository;
import com.example.onlybuns.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.example.onlybuns.exceptions.RateLimitExceededException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    @Autowired
    private FollowersRepository followersRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Autowired
    public UserInfoRepository userInfoRepository;

    @Autowired
    public PostRepository postRepository;

    private final ConcurrentMap<Long, Bucket> userBuckets = new ConcurrentHashMap<>();

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment); // Save the comment in the database
    }

    public List<CommentReadDto> getPostComments(Long postId){
        List<Comment> comments = commentRepository.getByPostId(postId);
        List<CommentReadDto> commentDtos = comments.stream().map(comment -> {
            CommentReadDto dto = new CommentReadDto();
            dto.setId(comment.getId());
            dto.setText(comment.getText());
            dto.setUsername(comment.getUser().getUsername()); // pristupa se user.username
            dto.setCreationDate(comment.getCreationDate());
            return dto;
        }).collect(Collectors.toList());
        return commentDtos;
    }

    private Bucket resolveBucket(Long userId) {
        return userBuckets.computeIfAbsent(userId, id -> {
            Refill refill = Refill.intervally(60, Duration.ofHours(1)); // 60 komentara na 1 sat
            Bandwidth limit = Bandwidth.classic(60, refill);
            return Bucket4j.builder().addLimit(limit).build();
        });
    }


    public Comment addComment(CommentCreationDto dto){
        Bucket bucket = resolveBucket(dto.getUserId());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Limit exceeded: maximum 60 comments per hour.");
        }

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Followers follow = followersRepository.findByFollower_IdAndFollowee_Id(dto.getUserId(), post.getUser().getId());
        if(follow == null){
            return null;
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setCreationDate(dto.getCreationDate());
        comment.setPost(post);
        UserInfo user = userInfoRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        comment.setUser(user);
        return commentRepository.save(comment);
    }
}

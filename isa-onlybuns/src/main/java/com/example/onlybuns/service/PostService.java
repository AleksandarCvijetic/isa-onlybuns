package com.example.onlybuns.service;

import com.example.onlybuns.model.Post;
import com.example.onlybuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import com.example.onlybuns.dtos.PostReadDto;
import com.example.onlybuns.model.Followers;
import com.example.onlybuns.repository.FollowersRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final FollowersRepository followersRepository;

    @Autowired
    public PostService(PostRepository postRepository, FollowersRepository followersRepository) {
        this.postRepository = postRepository;
        this.followersRepository = followersRepository;
    }

    public List<PostReadDto> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        List<PostReadDto> dtos = new ArrayList<>();

        for (Post post : posts) {
            PostReadDto dto = new PostReadDto();
            dto.setId(post.getId());
            dto.setDescription(post.getDescription());
            dto.setImage(post.getImage());
            dto.setCreatedAt(post.getCreatedAt());
            dto.setLikeCount(post.getLikeCount());
            dto.setCreatorUsername(post.getUser().getUsername());
            dto.setLocation(post.getLocation());

            dtos.add(dto);
        }

        return dtos;
    }

    @Cacheable("top10PostsCache")
    public List<Post> getTop10Posts(){
        return postRepository.findTop10ByOrderByLikeCountDesc();
    }

    @Cacheable("top5WeeklyPostsCache")
    public List<Post> getTop5PostsLast7Days() {
        ZonedDateTime sevenDaysAgo = ZonedDateTime.now().minusDays(7);
        return postRepository.findByCreatedAtAfterOrderByLikeCountDesc(sevenDaysAgo).stream().limit(5).collect(Collectors.toList());
    }


    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public List<Post> getPostsByUserId(Long userId){
        return postRepository.findByUser_Id(userId);
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
    public Post save(Post post) { return postRepository.save(post);}

    public List<PostReadDto> getFollowedUserPosts(Long userId) {
        List<Followers> follows = followersRepository.findByFollowerId(userId);
        List<Long> followeeIds = follows.stream()
                .map(follow -> follow.getFollowee().getId())
                .collect(Collectors.toList());
        List<Post> posts =  postRepository.getByUserIdIn(followeeIds);
        List<PostReadDto> dtos = posts.stream().map(post -> new PostReadDto(
                post.getId(),
                post.getDescription(),
                post.getImage(),
                post.getCreatedAt(),
                post.getLikeCount(),
                post.getUser().getUsername(),
                post.getLocation()
        )).toList();
        return dtos;
    }
}

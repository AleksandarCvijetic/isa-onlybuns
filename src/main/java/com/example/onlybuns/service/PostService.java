package com.example.onlybuns.service;

import com.example.onlybuns.model.Post;
import com.example.onlybuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
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
}

package com.example.onlybuns.controller;

import com.example.onlybuns.model.Post;
import com.example.onlybuns.service.PostService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

     // GET endpoint to fetch all posts
    @GetMapping(produces = "application/json")
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public Post createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }
}

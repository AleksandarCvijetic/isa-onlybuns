package com.example.onlybuns.controller;

import com.example.onlybuns.model.Location;
import com.example.onlybuns.model.Post;
import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.service.PostService;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.example.onlybuns.service.UserInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService postService;
    private final UserInfoService userInfoService;

    @Autowired
    public PostController(PostService postService, UserInfoService userInfoService) {
        this.postService = postService;
        this.userInfoService = userInfoService;
    }

     // GET endpoint to fetch all posts
    @GetMapping(produces = "application/json")
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @PostMapping(consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<Post> createPost(
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile image,
            @RequestParam("location") String locationString,
            @RequestParam("createdAt") String createdAt,
            @RequestParam("userId") Long userId) {

        try {
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean dirCreated = directory.mkdirs();
                if (!dirCreated) {
                    throw new IOException("Unable to create upload directory: " + uploadDir);
                }
            }

            String originalFilename = image.getOriginalFilename();
            String filePath = uploadDir + File.separator + UUID.randomUUID() + "_" + originalFilename;
            File dest = new File(filePath);
            image.transferTo(dest);

            Post post = new Post();
            post.setDescription(description);
            post.setImage(filePath);
            post.setCreatedAt(ZonedDateTime.parse(createdAt));


            UserInfo user = userInfoService.getUserById(userId);
            post.setUser(user);


            ObjectMapper objectMapper = new ObjectMapper();
            Location location = objectMapper.readValue(locationString, Location.class);
            post.setLocation(location);


            Post savedPost = postService.createPost(post);

            return ResponseEntity.ok(savedPost);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    }


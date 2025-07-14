package com.example.onlybuns.controller;

import com.example.onlybuns.dtos.PostReadDto;
import com.example.onlybuns.model.*;
import com.example.onlybuns.service.*;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/post")
public class PostController {
    private final PostService postService;
    private final UserInfoService userInfoService;
    private final CommentService commentService;
    private final LikeService likeService;

    @Autowired
    public PostController(PostService postService, UserInfoService userInfoService, CommentService commentService, LikeService likeService) {
        this.postService = postService;
        this.userInfoService = userInfoService;
        this.commentService = commentService;
        this.likeService = likeService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(post);
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
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body(null); 
            }
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
    // Add a comment to a post
    @PostMapping("/{postId}/comment")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId,
                                              @RequestBody Comment commentRequest) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Ensure that user is not null
        if (commentRequest.getUser() == null || commentRequest.getUser().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return bad request if user info is missing
        }

        UserInfo user = userInfoService.getUserById(commentRequest.getUser().getId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setText(commentRequest.getText());
        comment.setCreationDate(ZonedDateTime.now()); // Set current time as comment creation time

        Comment savedComment = commentService.createComment(comment); // Save comment to the database
        post.getComments().add(savedComment); // Add to the post's comments list

        return ResponseEntity.ok(savedComment); // Return the added comment
    }

    // Add a like to a

    @PostMapping("/{postId}/like")
    public ResponseEntity<Post> likePost(@PathVariable Long postId, @RequestBody Map<String, Long> requestBody) {
        Long userId = requestBody.get("userId");  // Extract userId from the request body

        Post post = postService.getPostById(postId);
        UserInfo user = userInfoService.getUserById(userId);

        if (post == null || user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Check if the user has already liked the post
        Like existingLike = likeService.getLikeByPostAndUser(postId, userId);
        if (existingLike != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(post); // User has already liked the post
        }

        // Create a new like
        Like like = new Like();
        like.setPost(post);
        like.setUser(user);

        likeService.createLike(user, post.getId()); // Create like in the database

        //post.incrementLikeCount(); // Increment the like count for the post
        //postService.save(post); // Save updated post with new like count

        return ResponseEntity.ok(post); // Return updated post with likes
    }
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id,
                                           @RequestBody Map<String, Object> requestBody) {
        // Get the existing post
        Post existingPost = postService.getPostById(id);

        if (existingPost == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Extract userId from request body
        if (!requestBody.containsKey("userId")) {
            return ResponseEntity.badRequest().body(null);
        }
        Long currentUserId = ((Number) requestBody.get("userId")).longValue();

        // Verify user ownership
        if (!existingPost.getUser().getId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        // Update fields
        String description = (String) requestBody.get("description");
        if (description != null) {
            existingPost.setDescription(description);
        }

        String image = (String) requestBody.get("image");
        if (image != null && !image.isEmpty()) {
            existingPost.setImage(image);
        }

        // If needed, you can handle other fields similarly
        // For example:
        // Map<String, Object> locationMap = (Map<String, Object>) requestBody.get("location");
        // ... Convert to Location object and set it if desired.

        // Save updated post
        Post savedPost = postService.save(existingPost);
        return ResponseEntity.ok(savedPost);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        // Get the existing post
        Post existingPost = postService.getPostById(id);

        Long currentUserId = ((UserInfoDetails) userDetails).getId();

        if (existingPost == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check ownership
        if (!existingPost.getUser().getId().equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Delete post
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followedUserPosts/{userId}")
    public List<PostReadDto> getAllPosts(@PathVariable Long userId) {
        return postService.getFollowedUserPosts(userId);
    }

}


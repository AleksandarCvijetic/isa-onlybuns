package com.example.onlybuns.controller;

import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.FollowersRepository;
import com.example.onlybuns.service.FollowersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/followers")
public class FollowersController {
    @Autowired
    private FollowersService followersService;

    @PostMapping("/follow")
    public ResponseEntity<String> followUser(@RequestBody Map<String, Long> payload) {
        Long followerId = payload.get("followerId");
        Long followeeId = payload.get("followeeId");
        followersService.followUser(followerId, followeeId);
        return ResponseEntity.ok("Successfully followed user");
    }
    @PostMapping("/unfollow")
    public ResponseEntity<String> unfollowUser(@RequestBody Map<String, Long> payload) {
        Long followerId = payload.get("followerId");
        Long followeeId = payload.get("followeeId");
        followersService.unfollowUser(followerId, followeeId);
        return ResponseEntity.ok("Successfully unfollowed user");
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<UserInfo>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followersService.getFollowers(userId));
    }

    @GetMapping("/followees/{userId}")
    public ResponseEntity<List<UserInfo>> getFollowees(@PathVariable Long userId) {
        return ResponseEntity.ok(followersService.getFollowees(userId));
    }
}

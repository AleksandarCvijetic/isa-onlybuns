package com.example.onlybuns.controller;

import com.example.onlybuns.dtos.AnalyticsDto;
import com.example.onlybuns.dtos.PostsCommentsDto;
import com.example.onlybuns.dtos.UserActivityDto;
import com.example.onlybuns.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/analytics")
public class AnalyticsController {
    @Autowired
    AnalyticsService analyticsService;

    @GetMapping
    public AnalyticsDto analytics() {
        return analyticsService.getAnalytics();
    }
    // for your Line charts
    @GetMapping("/posts-comments")
    public PostsCommentsDto postsComments(@RequestParam String period) {
        return analyticsService.getPostsComments(period);
    }

    // for your Doughnut
    @GetMapping("/user-activity")
    public UserActivityDto userActivity() {
        return analyticsService.getUserActivity();
    }
}


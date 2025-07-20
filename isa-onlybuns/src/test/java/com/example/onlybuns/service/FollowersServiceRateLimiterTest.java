package com.example.onlybuns.service;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest     // boots the full Spring context so the @RateLimiter proxy is in place
@EnableRetry        // preserves your retry behavior
class FollowersServiceRateLimiterTest {

    @Autowired
    FollowersService followersService;

    @Test
    void whenMoreThanFiveCalls_thenRateLimiterThrows() {
        long dummyFollowee = 999L;
        // First 5 calls should go through
        for (int i = 0; i < 5; i++) {
            followersService.followUser(i, dummyFollowee);
        }

        // 6th call within same minute window → immediately fails
        assertThatThrownBy(() ->
                followersService.followUser(42L, dummyFollowee)
        ).isInstanceOf(RequestNotPermitted.class);
    }
}

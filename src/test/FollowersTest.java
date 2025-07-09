package com.example.onlybuns;

import com.example.onlybuns.model.Followers;
import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.FollowersRepository;
import com.example.onlybuns.service.FollowersService;
import com.example.onlybuns.service.UserInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FollowersServiceConcurrencyTest {

    @Autowired
    private FollowersService followersService;

    @Autowired
    private FollowersRepository followersRepository;

    @Autowired
    private UserInfoService userInfoService;

    private UserInfo followee;
    private List<UserInfo> followers;

    @BeforeEach
    public void setUp() {
        // Create (or fetch) one followee user
        followee = new UserInfo();
        followee.setUsername("followeeUser");
        followee.setEmail("followee@example.com");
        followee.setPassword("password");
        userInfoService.saveUserInfo(followee);

        // Create multiple follower users
        followers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserInfo follower = new UserInfo();
            follower.setUsername("followerUser" + i);
            follower.setEmail("follower" + i + "@example.com");
            follower.setPassword("password");
            userInfoService.saveUserInfo(follower);
            followers.add(follower);
        }

        // Clean up any old test data in the followers repository (optional)
        followersRepository.deleteAll();
    }

    @Test
    public void testConcurrentFollows() throws InterruptedException {
        int threadCount = followers.size(); // 10 in this example
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // We'll store the Future results here
        List<Future<?>> futures = new ArrayList<>();

        // Submit a task for each follower
        for (UserInfo follower : followers) {
            Future<?> future = executorService.submit(() -> {
                // Each call runs in its own thread, in its own transaction
                followersService.followUser(follower.getId(), followee.getId());
            });
            futures.add(future);
        }

        // Wait for all threads to finish
        for (Future<?> future : futures) {
            try {
                future.get(); // if any thread throws, it will be re-thrown here
            } catch (ExecutionException e) {
                // If a runtime exception is thrown in followUser, handle it here
                e.printStackTrace();
            }
        }

        executorService.shutdown();

        // Now verify that the followee has the expected number of followers
        List<Followers> allFollows = followersRepository.findAll();
        long actualFollowerCount = allFollows
                .stream()
                .filter(f -> f.getFollowee().getId().equals(followee.getId()))
                .count();

        // We expect 10 distinct follow relationships
        Assertions.assertEquals(threadCount, actualFollowerCount,
                "The followee should have the correct number of followers.");

        // Optional: If your user info has a 'followerCount' or similar,
        // you can also assert that:
        //
        // int expectedCount = 10;
        // Assertions.assertEquals(expectedCount, followee.getFollowerCount());
    }
}

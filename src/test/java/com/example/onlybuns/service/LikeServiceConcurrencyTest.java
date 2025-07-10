package com.example.onlybuns.service;

import com.example.onlybuns.model.Post;
import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.PostRepository;
import com.example.onlybuns.repository.UserInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnableScheduling
@TestPropertySource(properties = {
        "user.cleanup.cron=-" // Disables the scheduled task
})
public class LikeServiceConcurrencyTest {
    @Autowired
    private LikeService likeService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserInfoRepository userRepository;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private UserCleanupService userCleanupService;

    private Post testPost;
    private UserInfo testUser;

    @BeforeEach
    void setUp() {
        // Create and save test data
        testUser = new UserInfo();
        testUser.setUsername("testuser");
        testUser = userRepository.save(testUser);

        testPost = new Post();
        testPost.setUser(testUser);
        testPost.setLikeCount(0);
        testPost = postRepository.save(testPost);
    }

    @Test
    void testConcurrentLikes() throws InterruptedException {
        int threadCount = 3;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            try {
                startLatch.await(); // Wait for all threads to be ready
                likeService.createLike(testUser, testPost.getId());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                endLatch.countDown();
            }
        };

        // Start threads
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.execute(task);
        }

        // Trigger all threads at once
        startLatch.countDown();

        // Wait for completion
        assertTrue(endLatch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify results
        Post updatedPost = postRepository.findById(testPost.getId()).orElseThrow();
        assertEquals(threadCount, updatedPost.getLikeCount());
    }
}

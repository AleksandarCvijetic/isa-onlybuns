package com.example.onlybuns.service;

import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.FollowersRepository;
import com.example.onlybuns.repository.UserInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EnableRetry
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({
        FollowersService.class,
        UserInfoService.class,
        FollowersServiceConcurrencyTest.TestConfig.class
})
class FollowersServiceConcurrencyTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
        @Bean
        JavaMailSender mailSender() {
            // no‐op stub
            return org.mockito.Mockito.mock(JavaMailSender.class);
        }
    }

    @Autowired FollowersService     followersService;
    @Autowired UserInfoService      userInfoService;
    @Autowired FollowersRepository  followersRepository;
    @Autowired UserInfoRepository   userInfoRepository;

    Long alice, bob, target;

    @BeforeEach
    void setUp() {
        followersRepository.deleteAll();
        userInfoRepository.deleteAll();

        alice  = saveUser("Alice",  "alice",  "a@x");
        bob    = saveUser("Bob",    "bob",    "b@x");
        target = saveUser("Carl",   "carl",   "c@x");
    }

    private Long saveUser(String name, String uname, String email) {
        UserInfo u = new UserInfo();
        u.setName(name);
        u.setUsername(uname);
        u.setEmail(email);
        u.setPassword("pw");
        return userInfoRepository.save(u).getId();
    }

    @Test
    void concurrentFollowsShouldBothSucceed() throws InterruptedException {
        int THREADS = 2;
        ExecutorService exec = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch go    = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(THREADS);

        Runnable r1 = () -> {
            ready.countDown();
            try {
                go.await();
                followersService.followUser(alice, target);
            } catch (Exception e) {
                // Log it so you actually see what went wrong
                e.printStackTrace();
            } finally {
                done.countDown();
            }
        };

        Runnable r2 = () -> {
            ready.countDown();
            try {
                go.await();
                followersService.followUser(bob, target);
            } catch (Exception ignore) {}
            done.countDown();
        };

        exec.submit(r1);
        exec.submit(r2);

        ready.await();   // wait for both threads to be ready
        go.countDown();  // let them go
        done.await();    // wait for both to finish

        assertThat(followersRepository.countByFollowee_Id(target))
                .as("both follow relationships should have been saved")
                .isEqualTo(2);
    }
}

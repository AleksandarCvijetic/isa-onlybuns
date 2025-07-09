package com.example.onlybuns.service;

import com.example.onlybuns.model.Followers;
import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.FollowersRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowersService {
    @Autowired
    private FollowersRepository followersRepository;

    @Autowired
    private UserInfoService userInfoService;

    @Retryable(
            value = OptimisticLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void followUser(long followerId, long followeeId) {
        if(followersRepository.existsByFollower_IdAndFollowee_Id(followerId, followeeId)) {
            throw new RuntimeException("Already following this user.");
        }
        if (followerId == followeeId) {
            throw new IllegalArgumentException("A user cannot follow themselves.");
        }

        UserInfo follower = userInfoService.getUserById(followerId);
        UserInfo followee = userInfoService.getUserById(followeeId);

        Followers follow = new Followers();
        follow.setFollower(follower);
        follow.setFollowee(followee);
        follow.setFollowedAt(ZonedDateTime.now());

        followersRepository.save(follow);
    }
    @Transactional
    public void unfollowUser(Long followerId, Long followeeId) {
        Followers follow = followersRepository.findByFollower_IdAndFollowee_Id(followerId, followeeId)
                .orElseThrow(() -> new RuntimeException("Follow relationship not found."));
        followersRepository.delete(follow);
    }
    public List<UserInfo> getFollowers(Long userId) {
        return followersRepository.findByFollowee_Id(userId).stream()
                .map(Followers::getFollower)
                .collect(Collectors.toList());
    }
    public List<UserInfo> getFollowees(Long userId) {
        return followersRepository.findByFollower_Id(userId).stream()
                .map(Followers::getFollowee)
                .collect(Collectors.toList());
    }
    @Recover
    public void recover(OptimisticLockException e, Long followerId, Long followeeId) {
        // fallback posle iscrpljenih retry-ja
        throw new RuntimeException("You can not follow now. Try again later!.", e);
    }

}

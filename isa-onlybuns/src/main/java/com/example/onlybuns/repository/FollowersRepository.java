package com.example.onlybuns.repository;

import com.example.onlybuns.model.Followers;
import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FollowersRepository extends JpaRepository<Followers, Long> {
    List<Followers> findByFollower_Id(long followerId);
    List<Followers> findByFollowee_Id(long followeeId);
    boolean existsByFollower_IdAndFollowee_Id(long followerId, long followee);
    long countByFollowee_Id(Long followeeId);

    @Query("SELECT COUNT(f) FROM Followers f WHERE f.followee.id = ?1 AND f.followedAt > ?2")
    long countNewFollowersForUserSinceLastLogin(Long userId, ZonedDateTime lastLogin);
    @Query("SELECT f.follower FROM Followers f WHERE f.followee.id = :userId")
    List<UserInfo> findFollowersOf(@Param("userId") Long userId);

    List<Followers> findByFollowerId(Long followerId);

    Followers findByFollower_IdAndFollowee_Id(Long followerId, Long followeeId);
}

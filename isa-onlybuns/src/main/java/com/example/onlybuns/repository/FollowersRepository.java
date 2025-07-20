package com.example.onlybuns.repository;

import com.example.onlybuns.model.Followers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowersRepository extends JpaRepository<Followers, Long> {
    List<Followers> findByFollower_Id(long followerId);
    List<Followers> findByFollowee_Id(long followeeId);
    boolean existsByFollower_IdAndFollowee_Id(long followerId, long followee);
    Optional<Followers> findByFollower_IdAndFollowee_Id(Long followerId, Long followeeId);
    long countByFollowee_Id(Long followeeId);
}

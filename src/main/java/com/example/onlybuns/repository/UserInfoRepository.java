package com.example.onlybuns.repository;

import com.example.onlybuns.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    Optional<UserInfo> findByEmail(String email); // Use 'email' if that is the correct field for login
    Optional<UserInfo> findByActivationToken(String token);
    Optional<UserInfo> findById(Long id);
    Optional<UserInfo> findByUsername(String username);
}


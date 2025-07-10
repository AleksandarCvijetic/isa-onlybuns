package com.example.onlybuns.repository;

import com.example.onlybuns.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer>, JpaSpecificationExecutor<UserInfo> {
    Optional<UserInfo> findByEmail(String email); // Use 'email' if that is the correct field for login
    Optional<UserInfo> findByActivationToken(String token);
    Optional<UserInfo> findById(Long id);
    Optional<UserInfo> findByUsername(String username);
    List<UserInfo> findByIsActiveFalseAndRegistrationDateBefore(LocalDateTime cutoffDate);
    @Modifying
    @Query("DELETE FROM UserInfo u WHERE u.isActive = false AND u.registrationDate < :cutoffDate")
    void deleteByIsActiveFalseAndRegistrationDateBefore(LocalDateTime cutoffDate);
    @Modifying
    @Query("UPDATE UserInfo u SET u.lastLogin = :lastLogin, u.isNotified = false WHERE u.id = :userId")
    void updateLastLoginByUserId(@Param("lastLogin") LocalDateTime lastLogin,
                                 @Param("userId") long id);
    List<UserInfo> findByLastLoginBeforeAndIsNotifiedFalse(LocalDateTime date);

    @Modifying
    @Query("UPDATE UserInfo u SET u.isNotified = true WHERE u.id = :userId")
    void markAsNotified(@Param("userId") Long userId);

}


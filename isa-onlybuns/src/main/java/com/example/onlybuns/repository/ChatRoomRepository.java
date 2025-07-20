package com.example.onlybuns.repository;

import com.example.onlybuns.model.ChatRoom;
import com.example.onlybuns.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByUsersContains(UserInfo user);
    @Query("""
       SELECT r
       FROM ChatRoom r
       WHERE r.group = false
         AND :u1 MEMBER OF r.users
         AND :u2 MEMBER OF r.users
    """)
    Optional<ChatRoom> findPrivateRoom(@Param("u1") UserInfo u1,
                                       @Param("u2") UserInfo u2);
}

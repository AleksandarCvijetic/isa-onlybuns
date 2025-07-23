package com.example.onlybuns.repository;

import com.example.onlybuns.model.ChatRoom;
import com.example.onlybuns.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    /**
     * Find every room where the given user is a key in membersJoinedAt.
     */
    @Query("""
    SELECT c
      FROM ChatRoom c
      JOIN c.membersJoinedAt m
     WHERE KEY(m) = :user
    """)
    List<ChatRoom> findByMember(@Param("user") UserInfo user);

    /**
     * Find an existing private‐chat (group=false) whose two members
     * are exactly u1 and u2.
     */
    @Query("""
    SELECT c
      FROM ChatRoom c
      JOIN c.membersJoinedAt m1
      JOIN c.membersJoinedAt m2
     WHERE c.group = false
       AND KEY(m1) = :u1
       AND KEY(m2) = :u2
    """)
    Optional<ChatRoom> findPrivateRoom(
            @Param("u1") UserInfo u1,
            @Param("u2") UserInfo u2
    );
}

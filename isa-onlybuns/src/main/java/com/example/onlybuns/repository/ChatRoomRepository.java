package com.example.onlybuns.repository;

import com.example.onlybuns.model.ChatRoom;
import com.example.onlybuns.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByUsersContains(UserInfo user);
    Optional<ChatRoom> findByIsGroupFalseAndUsersIn(List<UserInfo> users);
}

package com.example.onlybuns.repository;

import com.example.onlybuns.model.ChatMessageEntity;
import com.example.onlybuns.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findTop10ByChatRoomOrderByTimestampDesc(ChatRoom chatRoom);
    List<ChatMessageEntity> findByChatRoom_IdOrderByTimestampAsc(Long chatId);
}

package com.example.onlybuns.repository;

import com.example.onlybuns.model.ChatMessageEntity;
import com.example.onlybuns.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findTop10ByChatRoomOrderByTimestampDesc(ChatRoom chatRoom);
    List<ChatMessageEntity> findByChatRoom_IdOrderByTimestampAsc(Long chatId);
    List<ChatMessageEntity> findByChatRoom_IdAndTimestampGreaterThanEqualOrderByTimestampAsc(
            Long roomId,
            Instant joinedAt
    );
    List<ChatMessageEntity> findTop10ByChatRoom_IdAndTimestampBeforeOrderByTimestampDesc(Long roomId,Instant joined);
}

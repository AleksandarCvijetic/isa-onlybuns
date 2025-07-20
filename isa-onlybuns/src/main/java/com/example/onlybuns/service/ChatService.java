package com.example.onlybuns.service;

import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.model.ChatMessageEntity;
import com.example.onlybuns.model.ChatRoom;
import com.example.onlybuns.repository.ChatMessageRepository;
import com.example.onlybuns.repository.ChatRoomRepository;
import com.example.onlybuns.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepo;
    private final ChatMessageRepository chatMessageRepo;
    private final UserInfoRepository userRepo;

    public List<ChatRoom> getUserChats(String username) {
        UserInfo user = userRepo.findByUsername(username).orElseThrow();
        return chatRoomRepo.findByUsersContains(user);
    }

    public List<ChatMessageEntity> getLastMessages(Long roomId) {
        ChatRoom room = chatRoomRepo.findById(roomId).orElseThrow();
        List<ChatMessageEntity> messages = chatMessageRepo.findTop10ByChatRoomOrderByTimestampDesc(room);
        Collections.reverse(messages);
        return messages;
    }

    public ChatRoom startPrivateChat(String currentUser, String otherUsername) {
        UserInfo user1 = userRepo.findByUsername(currentUser).orElseThrow();
        UserInfo user2 = userRepo.findByUsername(otherUsername).orElseThrow();

        List<UserInfo> pair = List.of(user1, user2);
        Optional<ChatRoom> existing = chatRoomRepo.findByIsGroupFalseAndUsersIn(pair);
        if (existing.isPresent()) return existing.get();

        ChatRoom room = ChatRoom.builder()
                .isGroup(false)
                .users(new HashSet<>(pair))
                .build();
        return chatRoomRepo.save(room);
    }

    public ChatRoom createGroupChat(String adminUsername, Set<String> usernames, String groupName) {
        UserInfo admin = userRepo.findByUsername(adminUsername).orElseThrow();
        Set<UserInfo> users = new HashSet<>();
        for (String name : usernames) {
            userRepo.findByUsername(name).ifPresent(users::add);
        }
        users.add(admin);

        ChatRoom group = ChatRoom.builder()
                .isGroup(true)
                .name(groupName)
                .admin(admin)
                .users(users)
                .build();
        return chatRoomRepo.save(group);
    }

    public ChatMessageEntity saveMessage(String senderUsername, Long roomId, String content) {
        UserInfo sender = userRepo.findByUsername(senderUsername).orElseThrow();
        ChatRoom room = chatRoomRepo.findById(roomId).orElseThrow();

        ChatMessageEntity message = ChatMessageEntity.builder()
                .sender(sender)
                .content(content)
                .timestamp(Instant.now())
                .chatRoom(room)
                .build();
        return chatMessageRepo.save(message);
    }
}

package com.example.onlybuns.service;

import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.model.ChatMessageEntity;
import com.example.onlybuns.model.ChatRoom;
import com.example.onlybuns.repository.ChatMessageRepository;
import com.example.onlybuns.repository.ChatRoomRepository;
import com.example.onlybuns.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

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

    public ChatRoom startPrivateChat(String sender, String receiver) {
        log.info("Starting private chat");
        log.info("Sender: " + sender);
        log.info("Receiver: " + receiver);
        UserInfo u1 = userRepo.findByEmail(sender)          // ovde je bilo po username ali sender je email a receiver username!!!!!!
                .orElseThrow(() -> new UsernameNotFoundException(sender));
        UserInfo u2 = userRepo.findByUsername(receiver)
                .orElseThrow(() -> new UsernameNotFoundException(receiver));

        return chatRoomRepo.findPrivateRoom(u1, u2)
                .orElseGet(() -> {
                    ChatRoom room = new ChatRoom();
                    room.setGroup(false);
                    room.setUsers(Set.of(u1, u2));
                    return chatRoomRepo.save(room);
                });
    }

    public ChatRoom createGroupChat(String adminUsername, Set<String> usernames, String groupName) {
        UserInfo admin = userRepo.findByUsername(adminUsername).orElseThrow();
        Set<UserInfo> users = new HashSet<>();
        for (String name : usernames) {
            userRepo.findByUsername(name).ifPresent(users::add);
        }
        users.add(admin);

        ChatRoom group = ChatRoom.builder()
                .group(true)
                .name(groupName)
                .admin(admin)
                .users(users)
                .build();
        return chatRoomRepo.save(group);
    }

    public ChatMessageEntity saveMessage(String senderUsername, Long roomId, String content) {
        UserInfo sender = userRepo.findByEmail(senderUsername).orElseThrow();
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

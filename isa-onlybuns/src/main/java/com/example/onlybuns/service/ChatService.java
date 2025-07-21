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

import java.nio.file.AccessDeniedException;
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
        log.info("getUserChats username: " + username);
        UserInfo user = userRepo.findByEmail(username).orElseThrow();       //email??
        return chatRoomRepo.findByMember(user);
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
                    Instant now = Instant.now();
                    room.getMembersJoinedAt().put(u1,now);
                    room.getMembersJoinedAt().put(u2,now);
                    return chatRoomRepo.save(room);
                });
    }

    public ChatRoom createGroupChat(String adminUsername, Set<String> usernames, String groupName) {
        UserInfo admin = userRepo.findByEmail(adminUsername).orElseThrow();
        ChatRoom room = ChatRoom.builder()
                .name(groupName)
                .group(true)
                .admin(admin)
                .build();
        room.getMembersJoinedAt().put(admin, Instant.now());
        usernames.stream()
                .map(u -> userRepo.findByUsername(u).orElseThrow())
                .forEach(u -> room.getMembersJoinedAt().put(u, Instant.now()));
        return chatRoomRepo.save(room);
    }
    public void addUserToGroup(Long roomId, String adminUsername, String usernameToAdd) throws AccessDeniedException {
        ChatRoom room = chatRoomRepo.findById(roomId).orElseThrow();
        UserInfo admin = userRepo.findByEmail(adminUsername).orElseThrow();
        if(!room.isGroup() || !room.getAdmin().getUsername().equals(admin.getUsername())) {
            throw new AccessDeniedException("Only admin can add new users to group");
        }
        UserInfo user = userRepo.findByUsername(usernameToAdd).orElseThrow();
        room.getMembersJoinedAt().put(user, Instant.now());
        chatRoomRepo.save(room);
    }
    public List<ChatMessageEntity> getGroupMessagesForUser(String username, Long roomId) throws AccessDeniedException {
        ChatRoom room = chatRoomRepo.findById(roomId).orElseThrow();
        UserInfo user = userRepo.findByUsername(username).orElseThrow();
        Instant joined = room.getMembersJoinedAt().get(user);
        if(joined == null){
            throw new AccessDeniedException("not a member");
        }
        List<ChatMessageEntity> messagesBeforeJoin = chatMessageRepo.findTop10ByChatRoom_IdAndTimestampBeforeOrderByTimestampDesc(room.getId(),joined);
        Collections.reverse(messagesBeforeJoin);
        List<ChatMessageEntity> messagesAfterJoin = chatMessageRepo.findByChatRoom_IdAndTimestampGreaterThanEqualOrderByTimestampAsc(room.getId(),joined);
        List<ChatMessageEntity> history = new ArrayList<>(messagesBeforeJoin.size() + messagesAfterJoin.size());
        history.addAll(messagesBeforeJoin);
        history.addAll(messagesAfterJoin);
        return history;
    }
    public void removeUserFromGroup(Long roomId, String adminUsername, String usernameToRemove) throws AccessDeniedException {
        log.debug("admin username: " + adminUsername);
        log.debug("usernameToRemove: " + usernameToRemove);
        ChatRoom room = chatRoomRepo.findById(roomId).orElseThrow();
        UserInfo admin = userRepo.findByEmail(adminUsername).orElseThrow();
        log.debug("admin: " + admin.getUsername());
        if(!room.getAdmin().getUsername().equals(adminUsername)){
            throw new AccessDeniedException("Only admin can remove");
        }
        UserInfo user = userRepo.findByUsername(usernameToRemove).orElseThrow();
        Instant joined = room.getMembersJoinedAt().get(user);
        if(joined == null){
            throw new AccessDeniedException("not a member");
        }
        room.getMembersJoinedAt().remove(user);
        chatRoomRepo.save(room);
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
    public List<ChatMessageEntity> getAllMessagesForUser(Long roomId,String username) throws AccessDeniedException {
        ChatRoom room = chatRoomRepo.findById(roomId).orElseThrow();
        UserInfo user = userRepo.findByEmail(username).orElseThrow();
        if(room.isGroup()) return getGroupMessagesForUser(user.getUsername(),roomId);

        return chatMessageRepo.findByChatRoom_IdOrderByTimestampAsc(roomId);
    }
    public ChatRoom getRoom(Long roomId) {
        return chatRoomRepo.findById(roomId).orElseThrow();
    }

}

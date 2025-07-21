package com.example.onlybuns.controller;

import com.example.onlybuns.dtos.ChatMessageDto;
import com.example.onlybuns.dtos.ChatRoomDto;
import com.example.onlybuns.dtos.GroupCreateRequest;
import com.example.onlybuns.model.ChatMessageEntity;
import com.example.onlybuns.model.ChatRoom;
import com.example.onlybuns.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.onlybuns.model.UserInfo;


import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messaging;

    @GetMapping("/rooms")
    public List<ChatRoomDto> getUserRooms(Principal principal) {
        return chatService.getUserChats(principal.getName()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{roomId}/messages")
    public List<ChatMessageDto> getLastMessagesForUser(@PathVariable Long roomId,
                                                          Principal principal) throws AccessDeniedException {
        return chatService.getAllMessagesForUser(roomId, principal.getName())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/private/{username}")
    public ChatRoom startPrivate(@PathVariable String username,
                                 Principal principal) {

        // 1. kreiraj / dohvati sobu
        ChatRoom room = chatService.startPrivateChat(principal.getName(), username);

        // 2. pošalji push drugom učesniku
        //    • "/user/<username>/queue/new-chat"  (user-dest. prefix je "/user")
        messaging.convertAndSendToUser(
                username,          // kome šaljemo
                "/queue/new-chat", // destinacija (front subskribuje)
                room               // payload: možeš proslediti ceo ChatRoom ili DTO
        );

        return room;
    }

    @PostMapping("/group")
    public ChatRoomDto createGroup(@RequestBody GroupCreateRequest req, Principal principal) {
        ChatRoom room = chatService.createGroupChat(
                principal.getName(),
                req.getUsernames(),
                req.getName()
        );
        return toDto(room);
    }
    @PostMapping("/{roomId}/users")
    public ChatRoomDto addUserToRoom(Principal principal,@PathVariable Long roomId,@RequestBody Map<String,String> body) throws AccessDeniedException {
        String toAdd = body.get("username");
        chatService.addUserToGroup(roomId, principal.getName(), toAdd);
        messaging.convertAndSendToUser(
                toAdd, "/queue/new-chat",
                toDto(chatService.getRoom(roomId))
        );
        return toDto(chatService.getRoom(roomId));
    }
    @DeleteMapping("/{roomId}/users/{username}")
    public ChatRoomDto removeUserFromRoom(Principal principal,
                                          @PathVariable Long roomId,@PathVariable String username) throws AccessDeniedException {
        chatService.removeUserFromGroup(roomId,principal.getName(),username);
        messaging.convertAndSendToUser(
                username, "/queue/remove-from-chat",
                roomId
        );
        return toDto(chatService.getRoom(roomId));
    }


    @PostMapping("/{roomId}/message")
    public ChatMessageEntity sendMessage(@PathVariable Long roomId, @RequestBody Map<String, String> payload, Principal principal) {
        return chatService.saveMessage(principal.getName(), roomId, payload.get("content"));
    }


    private ChatRoomDto toDto(ChatRoom room) {
        Set<String> usernames = room.getMembersJoinedAt().keySet().stream()
                .map(UserInfo::getUsername)
                .collect(Collectors.toSet());
        String adminUsername = room.isGroup() && room.getAdmin() != null
                ? room.getAdmin().getUsername()
                : null;    // or "" if you prefer
        return new ChatRoomDto(
                room.getId(),
                room.getName(),
                room.isGroup(),
                adminUsername,
                usernames
        );
    }

    private ChatMessageDto toDto(ChatMessageEntity e) {
        return new ChatMessageDto(
                e.getId(),
                e.getSender().getUsername(),
                e.getContent(),
                e.getTimestamp()
        );
    }


}

package com.example.onlybuns.controller;

import com.example.onlybuns.model.ChatMessageEntity;
import com.example.onlybuns.model.ChatRoom;
import com.example.onlybuns.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    public List<ChatRoom> getUserRooms(Principal principal) {
        return chatService.getUserChats(principal.getName());
    }

    @GetMapping("/{roomId}/messages")
    public List<ChatMessageEntity> getLastMessages(@PathVariable Long roomId) {
        return chatService.getLastMessages(roomId);
    }

    @PostMapping("/private/{username}")
    public ChatRoom startPrivate(@PathVariable String username, Principal principal) {
        return chatService.startPrivateChat(principal.getName(), username);
    }

    @PostMapping("/group")
    public ChatRoom createGroup(@RequestBody Map<String, Object> payload, Principal principal) {
        String name = (String) payload.get("name");
        List<String> users = (List<String>) payload.get("usernames");
        return chatService.createGroupChat(principal.getName(), Set.copyOf(users), name);
    }

    @PostMapping("/{roomId}/message")
    public ChatMessageEntity sendMessage(@PathVariable Long roomId, @RequestBody Map<String, String> payload, Principal principal) {
        return chatService.saveMessage(principal.getName(), roomId, payload.get("content"));
    }
}

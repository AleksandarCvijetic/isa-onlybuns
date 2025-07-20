package com.example.onlybuns.controller;

import com.example.onlybuns.model.ChatMessage;
import com.example.onlybuns.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message) {
        // 1) Snimi u bazu
        chatService.saveMessage(
                message.getSender(),
                Long.parseLong(message.getChatId()),
                message.getContent()
        );

        // 2) Prosledi svim pretplaćenima
        message.setTimestamp(Instant.now());
        messagingTemplate.convertAndSend("/topic/" + message.getChatId(), message);
    }

    @MessageMapping("/chat.private")
    public void sendPrivate(@Payload ChatMessage message,
                            @Header("simpSessionAttributes") java.util.Map<String, Object> sessionAttributes) {
        message.setTimestamp(Instant.now());
        messagingTemplate.convertAndSendToUser(message.getSender(), "/queue/messages", message);
    }
}

package com.example.onlybuns.model;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    private String sender;
    private String content;
    private String chatId;
    private MessageType type;
    private Instant timestamp;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}

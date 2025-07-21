package com.example.onlybuns.dtos;

import lombok.*;
import java.time.Instant;

@Data
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private String senderUsername;
    private String content;
    private Instant timestamp;
}

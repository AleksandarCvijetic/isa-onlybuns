package com.example.onlybuns.dtos;

import lombok.*;
import java.util.Set;

@Data
@AllArgsConstructor
public class ChatRoomDto {
    private Long id;
    private String name;
    private boolean group;
    private String adminUsername;
    private Set<String> usernames;
}

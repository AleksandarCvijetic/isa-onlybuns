package com.example.onlybuns.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // null za privatne četove

    @Column(name = "is_group", nullable = false)
    private boolean group;

    @ElementCollection
    @CollectionTable(
            name="chat_room_users",
            joinColumns=@JoinColumn(name="chat_room_id")
    )
    @MapKeyJoinColumn(name="user_id")
    @Column(name="joined_at", nullable=false)
    @Builder.Default
    private Map<UserInfo, Instant> membersJoinedAt = new HashMap<>();

    @ManyToOne
    private UserInfo admin; // samo za grupne četove
}

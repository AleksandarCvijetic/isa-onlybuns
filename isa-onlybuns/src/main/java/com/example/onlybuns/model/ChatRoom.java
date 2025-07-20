package com.example.onlybuns.model;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToMany
    @JoinTable(
            name = "chat_room_users",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )

    private Set<UserInfo> users = new HashSet<>();

    @ManyToOne
    private UserInfo admin; // samo za grupne četove
}

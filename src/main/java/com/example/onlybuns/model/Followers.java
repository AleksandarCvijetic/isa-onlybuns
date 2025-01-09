package com.example.onlybuns.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Followers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "follower_id",nullable = false)
    private UserInfo follower;

    @ManyToOne
    @JoinColumn(name = "followee_id",nullable = false)
    private UserInfo followee;

    private ZonedDateTime followedAt;
}

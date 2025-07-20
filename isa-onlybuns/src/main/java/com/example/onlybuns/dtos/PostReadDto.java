package com.example.onlybuns.dtos;

import com.example.onlybuns.model.Comment;
import com.example.onlybuns.model.Location;
import com.example.onlybuns.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostReadDto {
    private Long id;
    private String description;
    private String image;
    private ZonedDateTime createdAt;
    private int likeCount;
    private String creatorUsername;
    private Location location;

}

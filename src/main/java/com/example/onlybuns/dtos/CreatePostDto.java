package com.example.onlybuns.dtos;


import com.example.onlybuns.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostDto {
    private String description;
    private String image;
    private ZonedDateTime createdAt;
    private Long userId;
    private Location location;
}

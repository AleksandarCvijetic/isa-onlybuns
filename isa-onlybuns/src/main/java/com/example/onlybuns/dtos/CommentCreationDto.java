package com.example.onlybuns.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreationDto {
    private String text;
    private Long userId;
    private ZonedDateTime creationDate;
    private Long postId;
}

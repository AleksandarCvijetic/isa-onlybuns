package com.example.onlybuns.dtos;

import com.example.onlybuns.model.Post;
import com.example.onlybuns.model.UserInfo;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentReadDto {
    private Long id;
    private String text;
    private String username;
    private ZonedDateTime creationDate;
}

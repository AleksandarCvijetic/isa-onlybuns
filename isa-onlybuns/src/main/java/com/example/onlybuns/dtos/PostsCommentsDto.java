package com.example.onlybuns.dtos;

import java.util.List;

public record PostsCommentsDto(
        List<String> labels,
        List<Long> posts,
        List<Long> comments
) {}

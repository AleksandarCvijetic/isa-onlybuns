package com.example.onlybuns.dtos;

public record UserActivityDto(
        double postedPct,
        double commentedPct,
        double inactivePct
) {}

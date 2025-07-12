package com.example.onlybuns.dtos;

import java.util.Map;

public class AnalyticsDto {
    public Map<String, Long> postsByPeriod;       // e.g. { "2025-06-01": 10, "2025-06-08": 12, … }
    public Map<String, Long> commentsByPeriod;
    public Map<String, Integer> userActivityBreakdown;
    // keys: "posted", "commentedOnly", "inactive"
}

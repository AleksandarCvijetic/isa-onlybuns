package com.example.onlybuns.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRateLimiter {
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<Long, Deque<Instant>> userRequestTimes = new ConcurrentHashMap<>();


    public boolean allowRequest(long userId) {
        Instant now = Instant.now();
        Deque<Instant> timestamps = userRequestTimes
                .computeIfAbsent(userId, id -> new ArrayDeque<>());

        synchronized (timestamps) {
            Instant cutoff = now.minus(WINDOW);
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < MAX_REQUESTS_PER_MINUTE) {
                timestamps.addLast(now);
                return true;
            } else {
                return false;
            }
        }
    }
}

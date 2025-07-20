package com.example.onlybuns.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveUserTracker {

    private final Map<String, Long> activeUserTimestamps = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public ActiveUserTracker(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        // Register gauge
        meterRegistry.gauge("daily_active_users", activeUserTimestamps, map -> map.size());
    }

    // Call this whenever a user is active (e.g. on any authenticated request)
    public void trackUserActivity(String userId) {
        activeUserTimestamps.put(userId, Instant.now().getEpochSecond());
    }

    // Scheduled cleanup: remove users who were last active > 24h ago
    @Scheduled(fixedRate = 60_000) // every 1 minute
    public void cleanupOldEntries() {
        long now = Instant.now().getEpochSecond();
        long threshold = now - 24 * 60 * 60; // 24 hours in seconds
        activeUserTimestamps.entrySet().removeIf(entry -> entry.getValue() < threshold);
    }
}

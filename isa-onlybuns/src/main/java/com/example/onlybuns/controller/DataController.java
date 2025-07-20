package com.example.onlybuns.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class DataController {

    // inject whichever port this instance is running on
    @Value("${server.port}")
    private int serverPort;

    /**
     * GET /api/data
     * Returns a JSON object containing the port and current timestamp.
     */
    @GetMapping("/api/data")
    public Map<String,Object> data() {
        return Map.of(
                "port",      serverPort,
                "timestamp", Instant.now().toString()
        );
    }
    @GetMapping("/api/slow")
    public Map<String,Object> slow() throws InterruptedException {
        Thread.sleep(5000);
        return Map.of("port", serverPort, "timestamp", Instant.now().toString());
    }

}

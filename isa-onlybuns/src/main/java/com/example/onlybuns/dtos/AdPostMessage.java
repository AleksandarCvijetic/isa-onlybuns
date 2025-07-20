package com.example.onlybuns.dtos;

public class AdPostMessage {
    private String description;
    private String timestamp;
    private String username;

    public AdPostMessage() {
    }

    public AdPostMessage(String description, String timestamp, String username) {
        this.description = description;
        this.timestamp = timestamp;
        this.username = username;
    }

    // Getters and setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
package com.example.onlybuns.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupCreateRequest {
    /** The name you want to give this new group chat */
    private String name;

    /** The set of usernames (e.g. “alice”, “bob”) to add as initial members */
    private Set<String> usernames;
}

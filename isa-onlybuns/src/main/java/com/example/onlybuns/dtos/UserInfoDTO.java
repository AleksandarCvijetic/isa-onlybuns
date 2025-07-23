package com.example.onlybuns.dtos;

import com.example.onlybuns.model.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String address;
    private long postCount; // Dynamically fetched

    public UserInfoDTO(UserInfo userInfo) {
        this.id = userInfo.getId();
        this.name = userInfo.getName();
        this.email = userInfo.getEmail();
        this.username = userInfo.getUsername();
        this.address = userInfo.getAddress();
        this.postCount = userInfo.getPostCount();
    }

    public UserInfoDTO(Long id, String username, String email) {
    }
}


package com.example.onlybuns.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private String username;
    private String address;
    private String roles;
    private String activationToken;
    private boolean isActive = false;

    @Formula("(select count(p.id) from post p where p.user_id = id)")
    private Long postCount;


//    @OneToMany(mappedBy = "user")
//    private List<Comment> comments = new ArrayList<>();

//    @OneToMany(mappedBy = "user")
//    private List<Post> posts = new ArrayList<>();

//    @OneToMany(mappedBy = "user")
//    private List<Like> likes = new ArrayList<>();
}

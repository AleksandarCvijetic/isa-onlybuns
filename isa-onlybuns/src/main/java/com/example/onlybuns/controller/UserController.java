package com.example.onlybuns.controller;

import com.example.onlybuns.dtos.UserInfoDTO;
import com.example.onlybuns.model.AuthRequest;
import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.service.JwtService;
import com.example.onlybuns.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("/auth")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserInfoService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @GetMapping("/getByUsername")
    public UserInfo getUserByUsername(@RequestBody UserInfo userInfo){
        return service.getUserByUsername(userInfo.getUsername());
    }
    @GetMapping("/getById/{id}")
    public UserInfo getUserById(@PathVariable long id){
        return service.getUserById(id);
    }

    @PostMapping("/addNewUser")
    public String addNewUser(@RequestBody UserInfo userInfo) {
        return service.addUser(userInfo);
    }

    @GetMapping("/activate")
    public String activateUser(@RequestParam("token") String token) {
        return service.activateUser(token);
    }

    @GetMapping("/user/userProfile")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public String userProfile() {
        return "Welcome to User Profile";
    }

    @GetMapping("/admin/adminProfile")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminProfile() {
        return "Welcome to Admin Profile";
    }

    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            UserInfo userInfo = service.getUserByEmail(authRequest.getEmail());
            service.updateLastLogin(userInfo);
            return jwtService.generateToken(authRequest.getEmail(), userInfo.getRoles(), userInfo.getId());
        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }
    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<UserInfoDTO> getAllUsers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "minPosts", required = false) Integer minPosts,
            @RequestParam(value = "maxPosts", required = false) Integer maxPosts,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size
    ) {
        logger.info("Sort by: {}, Sort order: {}", sortBy, sortOrder);

        return service.getFilteredUsers(name, email, minPosts, maxPosts, sortBy, sortOrder, page, size);
    }
    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public List<UserInfoDTO> getAllUsersForChat(Principal principal) {
        String currentUsername = principal.getName();
        return service.getAllUsersExcept(currentUsername);
    }




}


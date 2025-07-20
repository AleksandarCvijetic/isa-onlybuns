package com.example.onlybuns.controller;

import com.example.onlybuns.dtos.UserInfoDTO;
import com.example.onlybuns.dtos.ChangePasswordDto;
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
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    @Autowired
    private UserInfoService userInfoService;

    private Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> {
            Refill refill = Refill.greedy(5, Duration.ofMinutes(1)); // 5 pokušaja po 1 minuti
            Bandwidth limit = Bandwidth.classic(5, refill);
            return Bucket.builder().addLimit(limit).build();
        });
    }


    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @GetMapping("/users/{username}")
    public UserInfoDTO getUserByUsername(@PathVariable String username) {
        UserInfo user = service.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new UserInfoDTO(user);
    }

    @GetMapping("/userId/{id}")
    public UserInfoDTO getUserByUserId(@PathVariable Long id) {
        UserInfo user = service.getUserById(id);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + id);
        }
        return new UserInfoDTO(user);
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestBody ChangePasswordDto dto) {
        return service.changePassword(dto.getUserId(), dto.getOldPassword(), dto.getNewPassword());
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
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Bucket bucket = resolveBucket(ip);

        if (!bucket.tryConsume(1)) {
            logger.warn("Too many login attempts from IP: {}", ip);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts. Try again later.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                UserInfo userInfo = service.getUserByEmail(authRequest.getEmail());
                userInfoService.updateLastLogin(userInfo);
                return jwtService.generateToken(authRequest.getEmail(), userInfo.getRoles(), userInfo.getId());
            } else {
                throw new UsernameNotFoundException("Invalid user request!");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
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



}


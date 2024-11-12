package com.example.onlybuns.service;

import com.example.onlybuns.model.UserInfo;
import com.example.onlybuns.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Optional;

@Service
public class UserInfoService implements UserDetailsService {

    @Autowired
    private UserInfoRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Account not activated. Please check your email.");
        }

        return new UserInfoDetails(user);
    }


    public String addUser(UserInfo userInfo) {
        // Encode password
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));

        // Generate activation token
        String activationToken = UUID.randomUUID().toString();
        userInfo.setActivationToken(activationToken);
        userInfo.setActive(false);

        repository.save(userInfo);

        // Send verification email
        sendActivationEmail(userInfo.getEmail(), activationToken);

        return "Registration successful! Please check your email for the activation link.";
    }
    private void sendActivationEmail(String email, String activationToken) {
        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Account Activation");
        message.setText("Click the following link to activate your account: " + activationLink);
        message.setFrom(fromEmail);
        mailSender.send(message);
    }

    @Transactional
    public String activateUser(String token) {
        UserInfo user = repository.findByActivationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation token"));

        user.setActive(true);
        user.setActivationToken(null); // Clear token after activation
        repository.save(user);

        return "Account activated successfully!";
    }

    public UserInfo getUserByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    public UserInfo getUserById(Long userId) {
        return repository.findById(userId).orElse(null);
    }
}


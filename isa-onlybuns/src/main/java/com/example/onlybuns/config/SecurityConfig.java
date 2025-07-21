package com.example.onlybuns.config;

import com.example.onlybuns.filter.JwtAuthFilter;
import com.example.onlybuns.filter.LoginRateLimitingFilter;
import com.example.onlybuns.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.lang.Exception;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private LoginRateLimitingFilter loginRateLimitingFilter;

    @Autowired
    private JwtAuthFilter authFilter;

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserInfoService(); // Ensure UserInfoService implements UserDetailsService
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) Enable CORS
                .cors(Customizer.withDefaults())

                // 2) Rate-limit login requests before the UsernamePasswordAuthenticationFilter
                .addFilterBefore(loginRateLimitingFilter, UsernamePasswordAuthenticationFilter.class)

                // 3) Disable CSRF (stateless REST API)
                .csrf(AbstractHttpConfigurer::disable)

                // 4) URL authorization
                .authorizeHttpRequests(auth -> auth

                        // allow all OPTIONS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // public/auth endpoints
                        .requestMatchers(
                                "/auth/welcome",
                                "/auth/getByUsername",
                                "/auth/getById/**",
                                "/auth/addNewUser",
                                "/auth/activate/**",
                                "/auth/generateToken",
                                "/auth/users"
                        ).permitAll()

                        // chat endpoints
                        .requestMatchers(
                                "/api/chat/group",
                                "/api/chat/**",
                                HttpMethod.POST, "/api/chat/private/**"
                        ).permitAll()

                        // post-related public endpoints
                        .requestMatchers(
                                "/post",
                                "/post/**",
                                "/post/{postId}/like",
                                "/post/{postId}/comment",
                                "/post/top5weekly",
                                "/post/posts-by-user/{userId}",
                                "/post/posts-by-user/{userId}"
                        ).permitAll()

                        // images, followers, data, slow, health, ws, actuator
                        .requestMatchers(
                                "/images/{filename:.+}",
                                "/images/**",
                                "/followers/**",
                                "/api/data",
                                "/api/slow",
                                "/ws/**",
                                "/actuator/**"
                        ).permitAll()

                        // POSTs to private chat (if you want to allow even non-authenticated)
                        //.requestMatchers(HttpMethod.POST, "/api/chat/private/**").permitAll()

                        // secured deletion of posts
                        .requestMatchers(HttpMethod.DELETE, "/post/*").authenticated()

                        // USER-only endpoints
                        .requestMatchers(
                                "/auth/user/**",
                                "/post/followedUserPosts/**"
                        ).hasAuthority("ROLE_USER")

                        // ADMIN-only
                        .requestMatchers(
                                "/auth/admin/**",
                                "/admin/analytics/**",
                                "/post/{postId}/mark-for-advertising"
                        ).hasAuthority("ROLE_ADMIN")

                        // everything else needs auth
                        .anyRequest().authenticated()
                );

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}


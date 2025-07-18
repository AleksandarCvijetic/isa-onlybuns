package com.example.onlybuns.security;

import com.example.onlybuns.metrics.ActiveUserTracker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserActivityFilter extends OncePerRequestFilter {

    private final ActiveUserTracker tracker;

    public UserActivityFilter(ActiveUserTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            tracker.trackUserActivity(auth.getName());
        }

        filterChain.doFilter(request, response);
    }
}

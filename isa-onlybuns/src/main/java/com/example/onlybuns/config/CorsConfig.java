package com.example.onlybuns.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOrigin("http://localhost:5173"); // Vue.js frontend URL
        corsConfig.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*"); // Allow all HTTP methods (GET, POST, etc.)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Apply CORS configuration to all endpoints

        return new CorsFilter(source);
    }
}

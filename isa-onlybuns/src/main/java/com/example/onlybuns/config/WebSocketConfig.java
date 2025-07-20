package com.example.onlybuns.config;

import com.example.onlybuns.filter.JwtHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // za public (topic) i privatne (queue)
        registry.setApplicationDestinationPrefixes("/app"); // frontend šalje poruke ka /app/chat
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // STOMP endpoint
                .setAllowedOriginPatterns("*") // dev only
                .addInterceptors(new JwtHandshakeInterceptor())
                .withSockJS(); // fallback za starije browsere
    }
}

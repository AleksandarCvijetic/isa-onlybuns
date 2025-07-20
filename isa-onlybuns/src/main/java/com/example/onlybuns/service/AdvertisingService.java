package com.example.onlybuns.service;

import com.example.onlybuns.model.Post;
import com.example.onlybuns.dtos.AdPostMessage;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class AdvertisingService {

    private final AmqpTemplate amqpTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    public AdvertisingService(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendAdPostMessage(Post post) {
        AdPostMessage message = new AdPostMessage(
                post.getDescription(),
                post.getCreatedAt().toString(),
                post.getUser().getUsername()
        );

        amqpTemplate.convertAndSend(exchangeName, "", message); // fanout ignores routing key
    }
}
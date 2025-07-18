package com.example.onlybuns.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Imena exchange-a i queue-a (možeš iz properties preuzeti ako hoćeš)
    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    public static final String QUEUE_NAME = "agencyQueue1";

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(exchangeName, true, false);
    }

    @Bean
    public Queue agencyQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(FanoutExchange fanoutExchange, Queue agencyQueue) {
        return BindingBuilder.bind(agencyQueue).to(fanoutExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        return template;
    }
}

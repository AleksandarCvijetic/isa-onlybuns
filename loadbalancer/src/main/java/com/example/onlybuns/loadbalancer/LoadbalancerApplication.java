package com.example.onlybuns.loadbalancer;

import com.example.onlybuns.loadbalancer.config.LoadBalancerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
public class LoadbalancerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoadbalancerApplication.class, args);
    }
}

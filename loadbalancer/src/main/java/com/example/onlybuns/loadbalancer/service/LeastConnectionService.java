package com.example.onlybuns.loadbalancer.service;

import com.example.onlybuns.loadbalancer.config.LoadBalancerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LeastConnectionService {
    private static final Logger log = LoggerFactory.getLogger(LeastConnectionService.class);
    private final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();

    public LeastConnectionService(LoadBalancerProperties props) {
        props.getInstances().forEach(url -> {
            connectionCounts.put(url, new AtomicInteger(0));
            log.info("Registered backend instance {}", url);
        });
    }
    public String chooseInstance(){
        String chosen = connectionCounts.entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().get()))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("No backend instances configured"));

        log.info("Least-connection pick: {} ({} active)", chosen, connectionCounts.get(chosen).get());
        return chosen;
    }
    public void increment(String url){
        connectionCounts.get(url).incrementAndGet();
    }
    public void decrement(String url){
        connectionCounts.get(url).decrementAndGet();
    }

    public Map<String, AtomicInteger> getCounts() { return connectionCounts; }

}
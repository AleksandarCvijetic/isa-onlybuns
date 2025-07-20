package com.example.onlybuns.loadbalancer.service;

import com.example.onlybuns.loadbalancer.config.LoadBalancerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LeastConnectionService {
    private static final Logger log = LoggerFactory.getLogger(LeastConnectionService.class);
    private final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    private final Set<String> healthy = ConcurrentHashMap.newKeySet();

    public LeastConnectionService(LoadBalancerProperties props) {
        props.getInstances().forEach(url -> {
            connectionCounts.put(url, new AtomicInteger(0));
            healthy.add(url);
            log.info("Registered backend instance {}", url);
        });
    }
    public List<String> getAvailableInstances() {
        // sort healthy instances by ascending connection count
        return healthy.stream()
                .sorted(Comparator.comparingInt(u -> connectionCounts.get(u).get()))
                .toList();
    }
    public String chooseInstance(){
        String chosen = connectionCounts.entrySet().stream()
                .min(Comparator.comparingInt(e -> e.getValue().get()))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("No backend instances configured"));

        log.info("Least-connection pick: {} ({} active)", chosen, connectionCounts.get(chosen).get());
        return chosen;
    }
    /** Mark an instance UP again (e.g. after a successful health‐check) */
    public void markHealthy(String url) {
        healthy.add(url);
        log.info("Marked {} as healthy", url);
    }
    public void markUnhealthy(String url) {
        healthy.remove(url);
        log.info("Marked {} as unhealthy", url);
    }
    /** Check whether we consider this instance healthy right now */
    public boolean isHealthy(String url) {
        return healthy.contains(url);
    }
    public void increment(String url){
        connectionCounts.get(url).incrementAndGet();
    }
    public void decrement(String url){
        connectionCounts.get(url).decrementAndGet();
    }

    public Map<String, AtomicInteger> getCounts() { return connectionCounts; }

}
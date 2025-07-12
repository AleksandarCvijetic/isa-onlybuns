package com.example.onlybuns.loadbalancer.service;

import com.example.onlybuns.loadbalancer.config.LoadBalancerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HealthCheckService {
    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);
    private final LeastConnectionService balancer;
    private final RestTemplate restTemplate = new RestTemplate();
    private final LoadBalancerProperties props;

    public HealthCheckService(LeastConnectionService balancer,
                              LoadBalancerProperties props) {
        this.balancer = balancer;
        this.props    = props;
    }

    /**
     * Every 5 seconds: probe each configured instance’s /actuator/health.
     * Mark healthy if up, unhealthy if any error occurs.
     */
    @Scheduled(fixedDelayString = "${loadbalancer.health.interval:3000}")
    public void probeBackends() {
        for (String url : props.getInstances()) {
            String healthUrl = url + "/actuator/health";
            try {
                restTemplate.getForEntity(healthUrl, String.class);
                balancer.markHealthy(url);
            } catch (Exception ex) {
                log.warn("Health check failed for {}: {}", url, ex.getMessage());
                balancer.markUnhealthy(url);
            }
        }
    }
}

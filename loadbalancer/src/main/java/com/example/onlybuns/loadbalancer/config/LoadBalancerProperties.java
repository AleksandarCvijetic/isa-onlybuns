package com.example.onlybuns.loadbalancer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix="loadbalancer")
public class LoadBalancerProperties {
    private List<String> instances = new ArrayList<>();
    private Retry retry = new Retry();

    public List<String> getInstances() {
        return instances;
    }
    public void setInstances(List<String> instances) {
        this.instances = instances;
    }
    public Retry getRetry() {
        return retry;
    }
    public void setRetry(Retry retry) {
        this.retry = retry;
    }
    public static class Retry {
        private int maxAttempts = 3;
        private Backoff backoff = new Backoff();

        public int getMaxAttempts() {
           return maxAttempts;
        }
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
        public Backoff getBackoff() {
            return backoff;
        }
        public void setBackoff(Backoff backoff) {
            this.backoff = backoff;
        }
        public static class Backoff {
            private long delay;
            private double multiplier;

            public long getDelay() {
                return delay;
            }
            public void setDelay(long delay) {
                this.delay = delay;
            }
            public double getMultiplier() {
                return multiplier;
            }
            public void setMultiplier(double multiplier) {
                this.multiplier = multiplier;
            }

        }
    }
}
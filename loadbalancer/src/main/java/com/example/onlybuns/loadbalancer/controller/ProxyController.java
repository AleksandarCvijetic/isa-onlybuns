package com.example.onlybuns.loadbalancer.controller;

import com.example.onlybuns.loadbalancer.service.LeastConnectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@RestController
public class ProxyController {
    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);
    private final LeastConnectionService balancer;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public ProxyController(LeastConnectionService balancer) {
        this.balancer = balancer;
    }

    @Retryable(
            include = { RestClientException.class },      // or HttpServerErrorException.class
            exclude = { HttpClientErrorException.class }, // excludes 4xx
            maxAttemptsExpression = "#{@loadBalancerProperties.retry.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "#{@loadBalancerProperties.retry.backoff.delay}",
                    multiplierExpression = "#{@loadBalancerProperties.retry.backoff.multiplier}"
            )
    )
    @RequestMapping("/**")
    public ResponseEntity<byte[]> forward(HttpMethod method,
                                          HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        String uri = request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        for (String target : balancer.getAvailableInstances()) {
            balancer.increment(target);
            try {
                log.info("Forwarding {} {} → {}", method, uri, target);

                // Copy headers
                HttpHeaders headers = new HttpHeaders();
                Collections.list(request.getHeaderNames())
                        .forEach(name ->
                                headers.put(name, Collections.list(request.getHeaders(name)))
                        );

                HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

                // Send to backend
                ResponseEntity<byte[]> response = restTemplate.exchange(
                        target + uri, method, entity, byte[].class
                );

                // If backend itself failed with 5xx, mark it down and retry
                if (response.getStatusCode().is5xxServerError()) {
                    log.warn("Backend {} returned 5xx: {}", target, response.getStatusCode());
                    balancer.markUnhealthy(target);
                    throw new RestClientException("5xx from backend");
                }

                // 2xx and 4xx both just get returned directly
                return response;

            } catch (HttpClientErrorException clientErr) {
                // 4xx — client error (e.g. unauthorized): return it, do NOT drop instance
                log.warn("Received 4xx from {}: {}", target, clientErr.getStatusCode());
                return ResponseEntity
                        .status(clientErr.getStatusCode())
                        .headers(clientErr.getResponseHeaders())
                        .body(clientErr.getResponseBodyAsByteArray());

            } catch (RestClientException retryableErr) {
                // I/O errors or 5xx from above — mark instance down and try next
                log.warn("Instance {} failed (will be marked unhealthy): {}",
                        target, retryableErr.getMessage());
                balancer.markUnhealthy(target);

            } finally {
                balancer.decrement(target);
            }
        }

        // No healthy backends left
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Load‑balancer: all instances are down".getBytes());
    }

    @Recover
    public ResponseEntity<String> fallback(ExhaustedRetryException ex) {
        log.error("Retries exhausted for request", ex);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Load‑balancer: all retries failed: " + ex.getMessage());
    }
}

package com.example.onlybuns.loadbalancer;

import com.example.onlybuns.loadbalancer.config.LoadBalancerProperties;
import com.example.onlybuns.loadbalancer.service.LeastConnectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class LeastConnectionServiceTest {

    private LeastConnectionService svc;

    @BeforeEach
    void setUp() {
        // configure three instances
        LoadBalancerProperties props = new LoadBalancerProperties();
        props.setInstances(Arrays.asList(
                "http://one", "http://two", "http://three"
        ));

        svc = new LeastConnectionService(props);
    }

    @Test
    void choose_picksLowestCount() {
        // initially, all counts are zero → picks first in map order
        String first = svc.chooseInstance();
        assertThat(first).isIn("http://one", "http://two", "http://three");

        // simulate activity: increment “http://one” twice, “http://two” once
        svc.increment("http://one");
        svc.increment("http://one");
        svc.increment("http://two");

        // now counts: one=2, two=1, three=0 → choose “three”
        assertThat(svc.chooseInstance()).isEqualTo("http://three");
    }

    @Test
    void incrementAndDecrement_modifyCounts() {
        String url = svc.chooseInstance();
        svc.increment(url);

        // grab the underlying map via reflection (or re-expose for testing)
        AtomicInteger count = svc.getCounts().get(url);
        assertThat(count.get()).isOne();

        svc.decrement(url);
        assertThat(count.get()).isZero();
    }
}

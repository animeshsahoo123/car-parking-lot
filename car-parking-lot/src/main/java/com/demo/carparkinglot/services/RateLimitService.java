package com.demo.carparkinglot.services;

import com.demo.carparkinglot.configuration.params.RateLimitConfigParams;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final RateLimitConfigParams rateLimitConfigParams;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public boolean isRateExceeded(String remoteAddress) {
        return !resolveBucket(remoteAddress).tryConsume(1);
    }

    public Bucket resolveBucket(String remoteAddress) {
        return cache.computeIfAbsent(remoteAddress, this::newBucket);
    }

    private Bucket newBucket(String apiKey) {
        int reqPerMin = rateLimitConfigParams.getMaxRequestPerMinute();
        Bandwidth limit = Bandwidth.classic(reqPerMin, Refill.intervally(reqPerMin, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}

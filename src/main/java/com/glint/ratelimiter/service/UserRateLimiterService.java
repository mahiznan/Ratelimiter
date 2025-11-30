package com.glint.ratelimiter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserRateLimiterService implements UserRateLimiter {
    private final ConcurrentHashMap<String, TokenBucket> tokenBuckets = new ConcurrentHashMap<>();

    private final long refillRatePerSecond;

    private final long capacity;

    public UserRateLimiterService(@Value("${glint.ratelimiter.refillRatePerSecond}") long refillRatePerSecond, @Value("${glint.ratelimiter.capacity}") long capacity) {
        this.refillRatePerSecond = refillRatePerSecond;
        this.capacity = capacity;
    }

    @Override
    public boolean tryAcquire(String userId) {
        TokenBucket tokenBucket = getBucket(userId);
        return tokenBucket.tryAcquire();
    }

    @Override
    public void updateConfig(String userId, int newCapacity, double newRefillRatePerSecond) {
        TokenBucket tokenBucket = getBucket(userId);
        tokenBucket.updateConfig(newCapacity, newRefillRatePerSecond);
    }

    private TokenBucket getBucket(String userId) {
        return tokenBuckets.computeIfAbsent(userId, _ -> new TokenBucket(capacity, refillRatePerSecond));
    }

}


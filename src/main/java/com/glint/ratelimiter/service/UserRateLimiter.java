package com.glint.ratelimiter.service;

public interface UserRateLimiter {
    boolean tryAcquire(String userId);

    void updateConfig(String userId, int newCapacity, double newRefillRatePerSecond);
}

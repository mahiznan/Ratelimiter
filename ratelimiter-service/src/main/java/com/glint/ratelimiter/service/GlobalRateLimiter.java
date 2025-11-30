package com.glint.ratelimiter.service;

public interface GlobalRateLimiter {
    boolean tryAcquire();

    void updateConfig(int newCapacity, double newRefillRatePerSecond);

}

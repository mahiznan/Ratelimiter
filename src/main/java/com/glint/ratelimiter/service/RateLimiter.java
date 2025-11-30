package com.glint.ratelimiter.service;

public interface RateLimiter {
    boolean tryAcquire();

    void updateConfig(int newCapacity, double newRefillRatePerSecond);

}

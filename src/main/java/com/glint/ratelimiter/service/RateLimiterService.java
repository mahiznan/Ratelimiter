package com.glint.ratelimiter.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimiterService implements RateLimiter {
    private final AtomicLong tokens = new AtomicLong();

    private double refillRatePerSecond = 1.0;
    private long capacity = 5;
    private final AtomicLong lastRefillTime = new AtomicLong(System.nanoTime());

    public RateLimiterService() {
        tokens.set(capacity);
    }

    @Override
    public boolean tryAcquire() {
        refill();
        while (true) {
            long currentTokens = tokens.get();
            if (currentTokens <= 0) {
                return false;
            }
            if (tokens.compareAndSet(currentTokens, currentTokens - 1)) {
                return true;
            }
        }
    }

    final void refill() {
        long now = System.nanoTime();
        long last = lastRefillTime.get();
        long elapsed = now - last;
        long tokensToAdd = (long) (elapsed / 1_000_000_000.0 * refillRatePerSecond);
        if (tokensToAdd <= 0)
            return;
        if (lastRefillTime.compareAndSet(last, now)) {
            while (true) {
                long currentTokens = tokens.get();
                long newTokens = Math.min(capacity, currentTokens + tokensToAdd);
                if (tokens.compareAndSet(currentTokens, newTokens)) break;
            }
        }
    }

    @Override
    public void updateConfig(int newCapacity, double newRefillRatePerSecond) {
        this.capacity = newCapacity;
        this.refillRatePerSecond = newRefillRatePerSecond;
        tokens.set(newCapacity);
        lastRefillTime.set(System.nanoTime());
    }
}


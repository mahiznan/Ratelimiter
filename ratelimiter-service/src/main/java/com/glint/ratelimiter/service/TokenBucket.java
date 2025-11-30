package com.glint.ratelimiter.service;

public class TokenBucket {
    private long tokens;
    private double refillRatePerSecond;
    private long lastRefillTime;
    private final long capacity;

    TokenBucket(long capacity, long refillRatePerSecond) {
        this.tokens = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.capacity = capacity;
    }

    public synchronized boolean tryAcquire() {
        refill();
        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    final void refill() {
        long now = System.nanoTime();
        long elapsed = now - lastRefillTime;
        long tokensToAdd = (long) (elapsed / 1_000_000_000.0 * refillRatePerSecond);
        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }

    public void updateConfig(int newCapacity, double newRefillRatePerSecond) {
        this.tokens = newCapacity;
        this.refillRatePerSecond = newRefillRatePerSecond;
        lastRefillTime = System.nanoTime();
    }
}

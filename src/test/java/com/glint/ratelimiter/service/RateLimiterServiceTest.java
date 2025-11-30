package com.glint.ratelimiter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiter;

    @BeforeEach
    void setup() {
        this.rateLimiter = new RateLimiterService();
        rateLimiter.updateConfig(1001, 1.0);
    }

    @Test
    void testAcquireSuccess() {
        assertTrue(rateLimiter.tryAcquire());
    }

    @Test
    void testAcquireUntilEmpty() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryAcquire());
        }
        assertFalse(rateLimiter.tryAcquire());  // should be empty
    }

    @Test
    void testRefillAfterOneSecond() throws Exception {
        // empty bucket
        for (int i = 0; i < 5; i++) rateLimiter.tryAcquire();
        assertFalse(rateLimiter.tryAcquire());

        Thread.sleep(1100); // allow refill of 1 token

        assertTrue(rateLimiter.tryAcquire()); // should have 1 token now
    }

    @Test
    void testUpdateConfigReducesCapacity() {
        // bucket at full 5 tokens
        rateLimiter.updateConfig(2, 1.0);

        // should clamp token count to 2
        for (int i = 0; i < 2; i++)
            assertTrue(rateLimiter.tryAcquire());

        assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    void testUpdateConfigIncreasesCapacity() {
        rateLimiter.updateConfig(10, 5.0);

        for (int i = 0; i < 10; i++)
            assertTrue(rateLimiter.tryAcquire());
    }

    @Test
    void testRefillUsingNanoPrecision() throws Exception {
        rateLimiter.updateConfig(1, 2.0); // 2 tokens per second

        // empty bucket
        assertTrue(rateLimiter.tryAcquire());
        assertFalse(rateLimiter.tryAcquire());

        Thread.sleep(300); // 0.3 seconds → ~0 tokens added
        assertFalse(rateLimiter.tryAcquire());

        Thread.sleep(300); // total 0.6 sec → approx 1 token
        assertTrue(rateLimiter.tryAcquire());
    }


    @Test
    void testThreadSafetyUnderLoad() throws Exception {
        rateLimiter.updateConfig(20, 5.0);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger acquired = new AtomicInteger(0);

        for (int i = 0; i < 50; i++) {
            executor.submit(() -> {
                if (rateLimiter.tryAcquire()) {
                    acquired.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        // Cannot exceed capacity + a little refill from passing time
        assertTrue(acquired.get() <= 25);
    }

    @Test
    void testTryAcquirePerformanceUnder5ms() {
        // Warm up JVM (important for stable perf tests)
        for (int i = 0; i < 1000; i++) {
            rateLimiter.tryAcquire();
        }

        long start = System.nanoTime();
        boolean result = rateLimiter.tryAcquire();
        long end = System.nanoTime();

        long durationMs = (end - start) / 1_000_000;

        assertTrue(result);  // basic sanity check
        assertTrue(durationMs < 1,
                "tryAcquire() took too long: " + durationMs + " ms");
        System.out.println(durationMs + " ms");
    }


    @Test
    void testConcurrentRequests() throws InterruptedException {
        int THREAD_COUNT = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        Queue<Long> executionTimes = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                long start = System.nanoTime();
                try {
                    if (rateLimiter.tryAcquire()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } finally {
                    long durationMillis = (System.nanoTime() - start) / 1_000;
                    executionTimes.add(durationMillis);
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();
        long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double averageTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.printf("Min time (ms): %d, Max time (ms): %d%n", minTime, maxTime);
        System.out.printf("Average time (ms): %.2f%n", averageTime);
        System.out.println("Success count: " + successCount + ", Failure count: " + failureCount);
        assertEquals(THREAD_COUNT, successCount.get());
        assertEquals(0, failureCount.get());
    }


}
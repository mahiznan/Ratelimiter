package com.glint.ratelimiter.benchmark;

import com.glint.ratelimiter.service.UserRateLimiterService;
import org.openjdk.jmh.annotations.*;

public class UserRateLimiterBenchmark {
    private UserRateLimiterService userRateLimiterService;

    @Param({"1000"})
    private int userCount;

    private String[] userIds;

    @Setup(Level.Iteration)
    public void setup() {
        userRateLimiterService = new UserRateLimiterService(5, 1);
        userIds = new String[userCount];
        for (int i = 0; i < userCount; i++) {
            userIds[i] = "user-" + i;
        }
    }

    @Benchmark
    @Threads(Threads.MAX)
    public boolean testTryAcquire() {
        int idx = (int) (Math.random() * userCount);
        return userRateLimiterService.tryAcquire(userIds[idx]);
    }

    @Benchmark
    @Threads(Threads.MAX)
    public void testUpdateConfig() {
        int idx = (int) (Math.random() * userCount);
        userRateLimiterService.updateConfig(userIds[idx], 10, 2);
    }

}

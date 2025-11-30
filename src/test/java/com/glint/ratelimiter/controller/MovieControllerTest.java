package com.glint.ratelimiter.controller;

import com.glint.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
@Import(RateLimiterService.class)
class MovieControllerTest {

    private static final int THREAD_COUNT = 100;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void testConcurrentGetTopMovies() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        Queue<Long> executionTimes = new ConcurrentLinkedQueue<>();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            futures.add(executorService.submit(() -> {
                long start = System.nanoTime();
                try {
                    mockMvc.perform(get("/movies/top/5")
                                    .with(httpBasic("user", "password")))
                            .andExpect(status().isOk());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                    failureCount.incrementAndGet();
                } finally {
                    long durationMillis = (System.nanoTime() - start) / 1_000_000;
                    executionTimes.add(durationMillis);
                    latch.countDown();
                }
            }));
        }

        latch.await();
        executorService.shutdown();

        // Compute min, max, average
        long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double avgTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());
        System.out.println("Min time (ms): " + minTime);
        System.out.println("Max time (ms): " + maxTime);
        System.out.println("Average time (ms): " + avgTime);

        assertThat(successCount.get()).isEqualTo(THREAD_COUNT);
        assertThat(failureCount.get()).isEqualTo(0);


    }

}
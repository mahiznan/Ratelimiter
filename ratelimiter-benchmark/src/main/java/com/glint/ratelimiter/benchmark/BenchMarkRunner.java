package com.glint.ratelimiter.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchMarkRunner {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(UserRateLimiterBenchmark.class.getSimpleName())
                .forks(1) // number of separate JVM forks
                .warmupIterations(3) // warmup to stabilize JIT
                .measurementIterations(5) // actual measurement iterations
                .threads(8) // simulate 8 concurrent threads
                .build();

        new Runner(opt).run();
    }
}

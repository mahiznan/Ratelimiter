# Rate Limiter Spring Boot Service

## Problem Statement

High-traffic systems often require rate limiting to prevent abuse, ensure fair usage, and protect downstream services. The goal is to design and implement a **Token Bucket–based Rate Limiter** in a Spring Boot service that allows clients to:

* Acquire tokens at a regulated rate
* Dynamically update rate-limit configurations at runtime
* Handle burst traffic within configured capacity
* Maintain performance responsiveness (target: <5ms per request under load)
* Support concurrency and thread-safety

This service exposes APIs to check token availability and update rate limit configurations. Unit tests ensure correctness, performance, and behavior under concurrent load.

## Features

* **Token Bucket Algorithm** implementation
* **Thread-safe** token updates and reads
* **Configurable token capacity and refill rate**
* **Dynamic configuration update API**
* **Fast acquisition (<5ms)** ensured via tests
* **Parallel load testing** using JUnit + ExecutorService
* **Spring Boot Starter structure** for easy integration

## Architecture & Components

### 1. `TokenBucketRateLimiter`

This is the core implementation containing:

* `capacity` – total number of tokens allowed
* `refillRatePerSecond` – tokens added every second
* `currentTokens` – an atomic counter for token availability
* `lastRefillTimestamp` – tracks the last refill time

Key methods:

* `tryAcquire()` – checks/refills and consumes 1 token
* `updateConfig(int newCapacity, double newRefillRate)` – updates runtime limits
* `refillTokens()` – computes and applies refills

Uses `synchronized` blocks to ensure thread-safety.

### 2. Spring Boot Service Layer

The service wraps the limiter and exposes APIs like:

* `GET /ratelimit/try` – attempt to acquire a token
* `POST /ratelimit/update` – update bucket configuration

### 3. Unit Tests

Comprehensive tests include:

* Functional correctness (capacity, refill, config updates)
* Performance tests (<5ms response time)
* Concurrency tests (1000 users)

## Implementation Approach

### Token Bucket Algorithm

We use a **lazy refill** strategy:

* Refill is calculated **only when a request comes** (not on a background thread)
* Difference in timestamps determines refill quantity

### Thread-safety

* Update operations use a synchronized block
* Token count is stored in `double` or `int` depending on configuration
* Refill and acquire operations guard against race conditions

### Performance

* Designed for low-latency access
* Only simple arithmetic operations per call
* No external I/O or blocking code
* Micro-benchmark tests ensure <5ms execution

### Concurrency Testing

Using **1000 parallel threads**, we verify:

* No race conditions during acquisition
* No negative tokens
* System stays within configured limits

## Example Unit Tests

* Acquire tokens within capacity
* Reject requests after exhaustion
* Refill tokens after time elapse
* Config updates change runtime behavior
* 1000-thread concurrency correctness
* Performance test ensuring <5ms

## How to Run the Project

```bash
./mvnw clean install
./mvnw spring-boot:run
```

## Build & Test

```bash
./mvnw test
```

## API Endpoints

### `GET /ratelimit/try`

Returns:

```json
{
  "success": true | false
}
```

### `POST /ratelimit/update`

Input:

```json
{
  "capacity": 20,
  "refillRate": 5.0
}
```

## Future Enhancements

* Distributed rate limiting (Redis)
* Per-API / per-user rate limits
* Prometheus/Grafana metrics

## License

MIT License
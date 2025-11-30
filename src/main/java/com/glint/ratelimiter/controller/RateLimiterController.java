package com.glint.ratelimiter.controller;

import com.glint.ratelimiter.service.UserRateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ratelimiter")
public class RateLimiterController {

    private final UserRateLimiter userRateLimiter;

    public RateLimiterController(UserRateLimiter userRateLimiter) {
        this.userRateLimiter = userRateLimiter;
    }

    @PostMapping("/update")
    public String update(@RequestBody User user) {
        userRateLimiter.updateConfig(user.userId(), user.capacity(), user.refillRatePerSecond());
        return ResponseEntity.ok().build().toString();
    }

    public record User(String userId, int capacity, double refillRatePerSecond) {
    }
}

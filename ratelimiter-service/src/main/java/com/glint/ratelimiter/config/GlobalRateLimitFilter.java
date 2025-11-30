package com.glint.ratelimiter.config;

import com.glint.ratelimiter.service.GlobalRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class GlobalRateLimitFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(GlobalRateLimitFilter.class);

    private final GlobalRateLimiterService globalRateLimiterService;

    public GlobalRateLimitFilter(GlobalRateLimiterService globalRateLimiterService) {
        this.globalRateLimiterService = globalRateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            log.debug("Authorization header: {}", authHeader);
        }
        long startTime = System.nanoTime();
        boolean allowed = globalRateLimiterService.tryAcquire();
        long durationMs = (System.nanoTime() - startTime) / 1_000;
        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
            return;
        }
        response.addHeader("X-RateLimit-Latency", String.valueOf(durationMs));
        filterChain.doFilter(request, response);
    }
}
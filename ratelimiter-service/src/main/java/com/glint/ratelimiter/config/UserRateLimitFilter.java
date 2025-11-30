package com.glint.ratelimiter.config;

import com.glint.ratelimiter.service.UserRateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserRateLimitFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(UserRateLimitFilter.class);

    private final UserRateLimiterService userRateLimiterService;

    public UserRateLimitFilter(UserRateLimiterService userRateLimiterService) {
        this.userRateLimiterService = userRateLimiterService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("User-Id");
        String path = request.getRequestURI();
        if (path.startsWith("/api/ratelimiter/update")) {
            filterChain.doFilter(request, response); // skip
            return;
        }
        if (userId == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Unauthorized");
            return;
        }
        long startTime = System.nanoTime();
        boolean allowed = userRateLimiterService.tryAcquire(userId);
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
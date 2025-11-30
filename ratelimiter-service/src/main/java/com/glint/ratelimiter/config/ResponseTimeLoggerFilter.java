package com.glint.ratelimiter.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component
public class ResponseTimeLoggerFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ResponseTimeLoggerFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000; // convert to ms
            CompletableFuture.runAsync(() -> {
                String emoji;
                if (durationMs > 100) emoji = "🔥";        // slow
                else if (durationMs > 50) emoji = "⚠️";   // warning
                else emoji = "✅";                         // fast
                log.info("[{}] {} took {} ms", emoji, request.getMethod() + " " + request.getRequestURI(), durationMs);
            });
        }
    }
}

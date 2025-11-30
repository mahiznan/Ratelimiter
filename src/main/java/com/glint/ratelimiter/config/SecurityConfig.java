package com.glint.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;
    private final ResponseTimeLoggerFilter responseTimeLoggerFilter;

    public SecurityConfig(RateLimitFilter rateLimitFilter, ResponseTimeLoggerFilter responseTimeLoggerFilter) {
        this.rateLimitFilter = rateLimitFilter;
        this.responseTimeLoggerFilter = responseTimeLoggerFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated()
                ).httpBasic(Customizer.withDefaults());
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(responseTimeLoggerFilter, SecurityContextHolderFilter.class);
        return http.build();
    }
}

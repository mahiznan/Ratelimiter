package com.glint.ratelimiter.controller;

import com.glint.ratelimiter.config.SecurityConfig;
import com.glint.ratelimiter.service.UserRateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({RateLimiterController.class})
@Import({UserRateLimiterService.class, SecurityConfig.class})
class RateLimiterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testConfigUpdate() throws Exception {
        mockMvc.perform(post("/ratelimiter/update")
                .with(httpBasic("user", "password"))
                .header("User-Id", "rajesh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {
                                  "userId": "rajesh",
                                  "capacity": 10,
                                  "refillRatePerSecond": 1.0
                                }
                        """)
        ).andExpect(status().isOk());
    }
}
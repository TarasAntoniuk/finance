package com.tarasantoniuk.finance.security.auth.controller;

import com.tarasantoniuk.finance.security.auth.dto.AuthResponse;
import com.tarasantoniuk.finance.security.auth.ratelimit.ClientRateLimiter;
import com.tarasantoniuk.finance.security.auth.service.AuthService;
import com.tarasantoniuk.finance.security.config.SecurityExceptionHandler;
import com.tarasantoniuk.finance.security.jwt.service.JwtService;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerRateLimitTest {

    private static final int LIMIT_PER_PERIOD = 2;
    private static final String NOISY_CLIENT = "203.0.113.10";
    private static final String QUIET_CLIENT = "198.51.100.20";
    private static final String LOGIN_BODY = "{\"email\":\"user@example.com\",\"password\":\"SecureP@ss1\"}";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthService authService = mock(AuthService.class);
        when(authService.login(any())).thenReturn(new AuthResponse("access", "refresh"));

        RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();
        registry.rateLimiter("auth", RateLimiterConfig.custom()
                .limitForPeriod(LIMIT_PER_PERIOD)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build());

        AuthController controller = new AuthController(
                authService, mock(JwtService.class), new ClientRateLimiter(registry));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new SecurityExceptionHandler())
                .build();
    }

    @Test
    void login_WhenClientExceedsLimit_ShouldReturn429() throws Exception {
        for (int i = 0; i < LIMIT_PER_PERIOD; i++) {
            loginFrom(NOISY_CLIENT).andExpect(status().isOk());
        }

        loginFrom(NOISY_CLIENT)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void login_WhenAnotherClientExceededLimit_ShouldNotBeBlocked() throws Exception {
        for (int i = 0; i <= LIMIT_PER_PERIOD; i++) {
            loginFrom(NOISY_CLIENT);
        }

        loginFrom(QUIET_CLIENT).andExpect(status().isOk());
    }

    private ResultActions loginFrom(String clientAddress) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY)
                .with(request -> {
                    request.setRemoteAddr(clientAddress);
                    return request;
                }));
    }
}

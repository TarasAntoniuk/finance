package com.tarasantoniuk.finance.security.auth.ratelimit;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientRateLimiterTest {

    private static final int LIMIT_PER_PERIOD = 5;
    private static final String CLIENT_A = "203.0.113.10";
    private static final String CLIENT_B = "198.51.100.20";

    private ClientRateLimiter limiter;

    @BeforeEach
    void setUp() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(LIMIT_PER_PERIOD)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build();
        RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();
        registry.rateLimiter(ClientRateLimiter.AUTH_LIMITER_NAME, config);
        limiter = new ClientRateLimiter(registry);
    }

    @Test
    void acquirePermission_WhenClientExceedsLimit_ShouldThrow() {
        exhaust(CLIENT_A);

        assertThrows(RequestNotPermitted.class, () -> limiter.acquirePermission(CLIENT_A));
    }

    @Test
    void acquirePermission_WhenAnotherClientExceededLimit_ShouldStillAllow() {
        exhaust(CLIENT_A);

        assertDoesNotThrow(() -> limiter.acquirePermission(CLIENT_B));
    }

    private void exhaust(String clientAddress) {
        for (int i = 0; i < LIMIT_PER_PERIOD; i++) {
            limiter.acquirePermission(clientAddress);
        }
    }
}

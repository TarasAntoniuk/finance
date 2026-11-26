package com.tarasantoniuk.finance.security.auth.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.stereotype.Component;

/**
 * Rate limit for the public auth endpoints, one bucket per client address.
 * <p>
 * The limits come from the {@code resilience4j.ratelimiter.instances.auth} properties. A single shared
 * limiter would let one client lock login for everyone, so each client address gets its own limiter with
 * that configuration. Idle limiters are evicted once their refresh period has passed.
 */
@Component
public class ClientRateLimiter {

    static final String AUTH_LIMITER_NAME = "auth";
    private static final long MAX_TRACKED_CLIENTS = 10_000;

    private final RateLimiterConfig config;
    private final Cache<String, RateLimiter> limitersByClient;

    public ClientRateLimiter(RateLimiterRegistry registry) {
        this.config = registry.rateLimiter(AUTH_LIMITER_NAME).getRateLimiterConfig();
        this.limitersByClient = Caffeine.newBuilder()
                .expireAfterAccess(config.getLimitRefreshPeriod())
                .maximumSize(MAX_TRACKED_CLIENTS)
                .build();
    }

    /**
     * @throws RequestNotPermitted when the client has used up its permits for the current period
     */
    public void acquirePermission(String clientAddress) {
        RateLimiter limiter = limitersByClient.get(clientAddress,
                address -> RateLimiter.of(AUTH_LIMITER_NAME + ":" + address, config));
        if (!limiter.acquirePermission()) {
            throw RequestNotPermitted.createRequestNotPermitted(limiter);
        }
    }
}

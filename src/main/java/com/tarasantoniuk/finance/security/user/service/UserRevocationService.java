package com.tarasantoniuk.finance.security.user.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserRevocationService {

    private static final int ACCESS_TOKEN_TTL_MINUTES = 15;
    private static final int MAX_REVOKED_USERS = 10_000;

    private final Cache<Long, Boolean> revokedUsers;

    public UserRevocationService() {
        this.revokedUsers = Caffeine.newBuilder()
                .expireAfterWrite(ACCESS_TOKEN_TTL_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAX_REVOKED_USERS)
                .build();
    }

    public void revoke(Long userId) {
        revokedUsers.put(userId, Boolean.TRUE);
    }

    public boolean isRevoked(Long userId) {
        return revokedUsers.getIfPresent(userId) != null;
    }
}

package com.tarasantoniuk.finance.security.token.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tarasantoniuk.finance.security.token.entity.BlacklistedToken;
import com.tarasantoniuk.finance.security.token.repository.BlacklistedTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final int CACHE_TTL_MINUTES = 15;
    private static final int CACHE_MAX_SIZE = 10_000;

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final Cache<String, Boolean> cache;

    public TokenBlacklistService(BlacklistedTokenRepository blacklistedTokenRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                .maximumSize(CACHE_MAX_SIZE)
                .build();
    }

    @Transactional
    public void blacklist(String jti, LocalDateTime expiresAt) {
        if (!blacklistedTokenRepository.existsById(jti)) {
            blacklistedTokenRepository.save(new BlacklistedToken(jti, expiresAt));
        }
        cache.put(jti, Boolean.TRUE);
    }

    public boolean isBlacklisted(String jti) {
        if (cache.getIfPresent(jti) != null) {
            return true;
        }
        boolean present = blacklistedTokenRepository.existsById(jti);
        if (present) {
            cache.put(jti, Boolean.TRUE);
        }
        return present;
    }
}

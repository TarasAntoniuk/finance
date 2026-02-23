package com.tarasantoniuk.finance.security.token.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklist;

    public TokenBlacklistService() {
        this.blacklist = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    public void blacklist(String jti) {
        blacklist.put(jti, Boolean.TRUE);
    }

    public boolean isBlacklisted(String jti) {
        return blacklist.getIfPresent(jti) != null;
    }
}

package com.tarasantoniuk.finance.security.token.service;

import com.tarasantoniuk.finance.security.token.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class RefreshTokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupScheduler.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting refresh token cleanup");
        try {
            int deleted = refreshTokenRepository.deleteExpiredOrRevoked(LocalDateTime.now());
            log.info("Refresh token cleanup completed: {} tokens removed", deleted);
        } catch (Exception e) {
            log.error("Failed to cleanup refresh tokens", e);
        }
    }
}

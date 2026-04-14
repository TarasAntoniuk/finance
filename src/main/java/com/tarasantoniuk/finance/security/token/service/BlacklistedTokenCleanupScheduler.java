package com.tarasantoniuk.finance.security.token.service;

import com.tarasantoniuk.finance.security.token.repository.BlacklistedTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class BlacklistedTokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BlacklistedTokenCleanupScheduler.class);

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public BlacklistedTokenCleanupScheduler(BlacklistedTokenRepository blacklistedTokenRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Scheduled(cron = "0 15 3 * * *")
    @Transactional
    public void cleanupExpiredBlacklistedTokens() {
        log.info("Starting blacklisted token cleanup");
        try {
            int deleted = blacklistedTokenRepository.deleteExpired(LocalDateTime.now());
            log.info("Blacklisted token cleanup completed: {} tokens removed", deleted);
        } catch (Exception e) {
            log.error("Failed to cleanup blacklisted tokens", e);
        }
    }
}

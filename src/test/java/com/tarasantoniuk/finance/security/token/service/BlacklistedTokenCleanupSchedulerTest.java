package com.tarasantoniuk.finance.security.token.service;

import com.tarasantoniuk.finance.security.token.repository.BlacklistedTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistedTokenCleanupSchedulerTest {

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @InjectMocks
    private BlacklistedTokenCleanupScheduler scheduler;

    @Test
    void cleanupExpiredBlacklistedTokens_ShouldDeleteExpiredTokens() {
        when(blacklistedTokenRepository.deleteExpired(any(LocalDateTime.class))).thenReturn(5);

        scheduler.cleanupExpiredBlacklistedTokens();

        verify(blacklistedTokenRepository).deleteExpired(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredBlacklistedTokens_WhenRepositoryFails_ShouldNotPropagateException() {
        when(blacklistedTokenRepository.deleteExpired(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("database unavailable"));

        assertDoesNotThrow(() -> scheduler.cleanupExpiredBlacklistedTokens());

        verify(blacklistedTokenRepository).deleteExpired(any(LocalDateTime.class));
    }
}

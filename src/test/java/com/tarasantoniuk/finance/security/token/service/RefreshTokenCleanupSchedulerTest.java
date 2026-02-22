package com.tarasantoniuk.finance.security.token.service;

import com.tarasantoniuk.finance.security.token.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupSchedulerTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenCleanupScheduler scheduler;

    @Test
    void cleanupExpiredTokens_WhenTokensExist_ShouldDeleteThem() {
        when(refreshTokenRepository.deleteExpiredOrRevoked(any(LocalDateTime.class))).thenReturn(5);

        scheduler.cleanupExpiredTokens();

        verify(refreshTokenRepository).deleteExpiredOrRevoked(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredTokens_WhenNoTokensToDelete_ShouldCompleteSuccessfully() {
        when(refreshTokenRepository.deleteExpiredOrRevoked(any(LocalDateTime.class))).thenReturn(0);

        scheduler.cleanupExpiredTokens();

        verify(refreshTokenRepository).deleteExpiredOrRevoked(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredTokens_WhenRepositoryThrowsException_ShouldHandleGracefully() {
        when(refreshTokenRepository.deleteExpiredOrRevoked(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database connection lost"));

        scheduler.cleanupExpiredTokens();

        verify(refreshTokenRepository).deleteExpiredOrRevoked(any(LocalDateTime.class));
    }
}

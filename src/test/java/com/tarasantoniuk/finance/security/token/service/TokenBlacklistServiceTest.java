package com.tarasantoniuk.finance.security.token.service;

import com.tarasantoniuk.finance.security.token.entity.BlacklistedToken;
import com.tarasantoniuk.finance.security.token.repository.BlacklistedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    private TokenBlacklistService tokenBlacklistService;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(blacklistedTokenRepository);
        expiresAt = LocalDateTime.now().plusMinutes(15);
    }

    @Test
    void isBlacklisted_WhenTokenNotBlacklisted_ShouldReturnFalse() {
        when(blacklistedTokenRepository.existsById("some-jti")).thenReturn(false);

        assertFalse(tokenBlacklistService.isBlacklisted("some-jti"));
    }

    @Test
    void isBlacklisted_WhenTokenInCache_ShouldReturnTrueWithoutExtraDbHit() {
        when(blacklistedTokenRepository.existsById("some-jti")).thenReturn(false);
        tokenBlacklistService.blacklist("some-jti", expiresAt);

        assertTrue(tokenBlacklistService.isBlacklisted("some-jti"));
        // existsById was called once inside blacklist(), but isBlacklisted() served from cache
        verify(blacklistedTokenRepository, org.mockito.Mockito.times(1)).existsById("some-jti");
    }

    @Test
    void isBlacklisted_WhenTokenOnlyInDb_ShouldReturnTrueAndCache() {
        when(blacklistedTokenRepository.existsById("persisted-jti")).thenReturn(true);

        assertTrue(tokenBlacklistService.isBlacklisted("persisted-jti"));
        assertTrue(tokenBlacklistService.isBlacklisted("persisted-jti"));
        verify(blacklistedTokenRepository).existsById("persisted-jti");
    }

    @Test
    void blacklist_ShouldPersistToDb() {
        when(blacklistedTokenRepository.existsById("jti-1")).thenReturn(false);

        tokenBlacklistService.blacklist("jti-1", expiresAt);

        verify(blacklistedTokenRepository).save(any(BlacklistedToken.class));
    }

    @Test
    void blacklist_WhenAlreadyPersisted_ShouldNotDuplicate() {
        when(blacklistedTokenRepository.existsById("jti-1")).thenReturn(true);

        tokenBlacklistService.blacklist("jti-1", expiresAt);

        verify(blacklistedTokenRepository, never()).save(any(BlacklistedToken.class));
    }
}

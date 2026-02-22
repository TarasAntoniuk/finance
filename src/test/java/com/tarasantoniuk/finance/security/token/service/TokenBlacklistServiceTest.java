package com.tarasantoniuk.finance.security.token.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    void isBlacklisted_WhenTokenNotBlacklisted_ShouldReturnFalse() {
        assertFalse(tokenBlacklistService.isBlacklisted("some-jti"));
    }

    @Test
    void isBlacklisted_WhenTokenBlacklisted_ShouldReturnTrue() {
        tokenBlacklistService.blacklist("some-jti");
        assertTrue(tokenBlacklistService.isBlacklisted("some-jti"));
    }

    @Test
    void blacklist_ShouldOnlyAffectBlacklistedToken() {
        tokenBlacklistService.blacklist("jti-1");

        assertTrue(tokenBlacklistService.isBlacklisted("jti-1"));
        assertFalse(tokenBlacklistService.isBlacklisted("jti-2"));
    }

    @Test
    void blacklist_WhenCalledMultipleTimes_ShouldStillBeBlacklisted() {
        tokenBlacklistService.blacklist("jti-1");
        tokenBlacklistService.blacklist("jti-1");

        assertTrue(tokenBlacklistService.isBlacklisted("jti-1"));
    }
}

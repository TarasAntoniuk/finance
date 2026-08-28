package com.tarasantoniuk.finance.security.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRevocationServiceTest {

    private UserRevocationService userRevocationService;

    @BeforeEach
    void setUp() {
        userRevocationService = new UserRevocationService();
    }

    @Test
    void isRevoked_WhenUserWasRevoked_ShouldReturnTrue() {
        userRevocationService.revoke(1L);

        assertTrue(userRevocationService.isRevoked(1L));
    }

    @Test
    void isRevoked_WhenUserWasNeverRevoked_ShouldReturnFalse() {
        assertFalse(userRevocationService.isRevoked(1L));
    }

    @Test
    void isRevoked_ShouldOnlyAffectTheRevokedUser() {
        userRevocationService.revoke(1L);

        assertTrue(userRevocationService.isRevoked(1L));
        assertFalse(userRevocationService.isRevoked(2L));
    }
}

package com.tarasantoniuk.finance.security.jwt;

import com.tarasantoniuk.finance.security.user.enums.UserRole;

public record JwtPrincipal(Long userId, String email, UserRole role, Long organizationId) {
}

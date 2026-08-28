package com.tarasantoniuk.finance.security.auth.google;

public record GoogleUserInfo(String email, boolean emailVerified, String name, String subject) {
}

package com.tarasantoniuk.finance.security.auth.validation;

public final class PasswordConstraints {

    public static final String PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()\\-_=+])[A-Za-z\\d@$!%*?&#^()\\-_=+]{8,}$";
    public static final String MESSAGE = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character";

    private PasswordConstraints() {
    }
}

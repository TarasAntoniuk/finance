package com.tarasantoniuk.finance.security.auth.exception;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException(String message) {
        super(message);
    }
}

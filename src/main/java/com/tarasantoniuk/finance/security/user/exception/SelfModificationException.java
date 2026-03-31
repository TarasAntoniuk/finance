package com.tarasantoniuk.finance.security.user.exception;

public class SelfModificationException extends RuntimeException {

    public SelfModificationException(String message) {
        super(message);
    }
}

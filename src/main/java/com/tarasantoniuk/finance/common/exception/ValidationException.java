package com.tarasantoniuk.finance.common.exception;

/**
 * Base exception for validation errors
 */
class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
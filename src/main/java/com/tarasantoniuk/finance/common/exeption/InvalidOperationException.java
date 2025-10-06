package com.tarasantoniuk.finance.common.exeption;

/**
 * Base exception for invalid operation errors
 */
class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}

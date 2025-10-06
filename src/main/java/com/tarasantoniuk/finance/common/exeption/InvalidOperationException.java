package com.tarasantoniuk.finance.common.exeption;

/**
 * Base exception for invalid operation errors
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}

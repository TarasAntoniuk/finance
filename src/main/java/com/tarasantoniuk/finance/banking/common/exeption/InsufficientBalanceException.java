package com.tarasantoniuk.finance.banking.common.exeption;

/**
 * Exception thrown when trying to post a payment with insufficient account balance
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
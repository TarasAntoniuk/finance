package com.tarasantoniuk.finance.counterparty.exception;

/**
 * Exception thrown when attempting to create a counterparty with duplicate code
 */
public class DuplicateCounterpartyException extends RuntimeException {

    public DuplicateCounterpartyException(String code) {
        super(String.format("Counterparty with code already exists: %s", code));
    }

    public DuplicateCounterpartyException(String message, Object... args) {
        super(String.format(message, args));
    }

    // Static factory method
    public static DuplicateCounterpartyException byCode(String code) {
        return new DuplicateCounterpartyException(code);
    }
}
package com.tarasantoniuk.finance.banking.bank.exception;

/**
 * Exception thrown when attempting to create a bank with duplicate SWIFT code
 */
public class DuplicateBankException extends RuntimeException {

    public DuplicateBankException(String swiftCode) {
        super(String.format("Bank with SWIFT code already exists: %s", swiftCode));
    }

    public DuplicateBankException(String message, Object... args) {
        super(String.format(message, args));
    }

    // Static factory method
    public static DuplicateBankException bySwiftCode(String swiftCode) {
        return new DuplicateBankException(swiftCode);
    }
}
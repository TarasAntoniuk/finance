package com.tarasantoniuk.finance.banking.bank.exception;

import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a bank is not found
 */
public class BankNotFoundException extends ResourceNotFoundException {

    public BankNotFoundException(Long id) {
        super("Bank", "id", id);
    }

    public BankNotFoundException(String message) {
        super(message);
    }

    // Static factory methods
    public static BankNotFoundException byId(Long id) {
        return new BankNotFoundException(id);
    }

    public static BankNotFoundException bySwiftCode(String swiftCode) {
        return new BankNotFoundException(
                String.format("Bank not found with SWIFT code: %s", swiftCode)
        );
    }
}
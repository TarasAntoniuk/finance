package com.tarasantoniuk.finance.banking.bankaccount.exception;

/**
 * Exception thrown when attempting to create a bank account with duplicate account number
 */
public class DuplicateBankAccountException extends RuntimeException {

    public DuplicateBankAccountException(String accountNumber) {
        super(String.format("Bank account with account number already exists: %s", accountNumber));
    }

    public DuplicateBankAccountException(String message, Object... args) {
        super(String.format(message, args));
    }

    // Static factory method
    public static DuplicateBankAccountException byAccountNumber(String accountNumber) {
        return new DuplicateBankAccountException(accountNumber);
    }
}
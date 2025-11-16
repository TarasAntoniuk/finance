package com.tarasantoniuk.finance.bankaccount.exception;


import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a bank account is not found
 */
public class BankAccountNotFoundException extends ResourceNotFoundException {

    public BankAccountNotFoundException(Long id) {
        super("BankAccount", "id", id);
    }

    public BankAccountNotFoundException(String message) {
        super(message);
    }

    // Static factory methods
    public static BankAccountNotFoundException byId(Long id) {
        return new BankAccountNotFoundException(id);
    }

    public static BankAccountNotFoundException byAccountNumber(String accountNumber) {
        return new BankAccountNotFoundException(
                String.format("Bank account not found with account number: %s", accountNumber)
        );
    }
}
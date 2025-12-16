package com.tarasantoniuk.finance.banking.bankaccounttransaction.enums;

/**
 * Bank account transaction type for Event Sourcing
 */
public enum TransactionType {
    /**
     * Debit - money received into the bank account
     */
    DEBIT,

    /**
     * Credit - money paid out from the bank account
     */
    CREDIT
}
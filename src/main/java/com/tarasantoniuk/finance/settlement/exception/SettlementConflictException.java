package com.tarasantoniuk.finance.settlement.exception;

/**
 * A document cannot be reversed while other documents have settled against it, e.g. an invoice
 * with payments allocated to it. Those documents must be reversed first.
 */
public class SettlementConflictException extends RuntimeException {

    public SettlementConflictException(String message) {
        super(message);
    }
}

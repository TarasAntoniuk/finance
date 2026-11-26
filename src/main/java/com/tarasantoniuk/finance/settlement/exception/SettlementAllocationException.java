package com.tarasantoniuk.finance.settlement.exception;

import com.tarasantoniuk.finance.common.exception.ValidationException;

/**
 * An explicit payment allocation does not fit the open items (unknown document, other
 * counterparty or currency, more than is open, more than the payment).
 */
public class SettlementAllocationException extends ValidationException {

    public SettlementAllocationException(String message) {
        super(message);
    }
}

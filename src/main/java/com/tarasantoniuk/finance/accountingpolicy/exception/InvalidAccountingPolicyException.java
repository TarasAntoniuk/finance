package com.tarasantoniuk.finance.accountingpolicy.exception;

import com.tarasantoniuk.finance.common.exception.InvalidOperationException;

/**
 * Exception thrown when invalid accounting policy operation is attempted
 */
public class InvalidAccountingPolicyException extends InvalidOperationException {

    public InvalidAccountingPolicyException(String message) {
        super(message);
    }
}
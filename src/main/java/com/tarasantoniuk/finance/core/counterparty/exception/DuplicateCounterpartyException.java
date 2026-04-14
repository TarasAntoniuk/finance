package com.tarasantoniuk.finance.core.counterparty.exception;

import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;

/**
 * Exception thrown when attempting to create a counterparty with duplicate code
 */
public class DuplicateCounterpartyException extends ResourceAlreadyExistsException {

    public DuplicateCounterpartyException(String code) {
        super("Counterparty", "code", code);
    }

    // Static factory method
    public static DuplicateCounterpartyException byCode(String code) {
        return new DuplicateCounterpartyException(code);
    }
}

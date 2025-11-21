package com.tarasantoniuk.finance.core.currency.exception;

import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;

/**
 * Exception thrown when currency numeric code already exists
 */
public class CurrencyNumericCodeAlreadyExistsException extends ResourceAlreadyExistsException {

    public CurrencyNumericCodeAlreadyExistsException(String numericCode) {
        super("Currency", "numericCode", numericCode);
    }
}
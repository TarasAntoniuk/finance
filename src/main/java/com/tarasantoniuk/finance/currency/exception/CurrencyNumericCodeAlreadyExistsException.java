package com.tarasantoniuk.finance.currency.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceAlreadyExistsException;

/**
 * Exception thrown when currency numeric code already exists
 */
class CurrencyNumericCodeAlreadyExistsException extends ResourceAlreadyExistsException {

    public CurrencyNumericCodeAlreadyExistsException(String numericCode) {
        super("Currency", "numericCode", numericCode);
    }
}
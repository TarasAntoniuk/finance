package com.tarasantoniuk.finance.currency.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceAlreadyExistsException;

/**
        * Exception thrown when a currency already exists
 */
class CurrencyAlreadyExistsException extends ResourceAlreadyExistsException {

    public CurrencyAlreadyExistsException(String code) {
        super("Currency", "code", code);
    }

    public CurrencyAlreadyExistsException(String fieldName, String fieldValue) {
        super("Currency", fieldName, fieldValue);
    }
}
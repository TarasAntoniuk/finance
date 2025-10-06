package com.tarasantoniuk.finance.currency.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceNotFoundException;

/**
        * Exception thrown when a currency is not found
 */
public class CurrencyNotFoundException extends ResourceNotFoundException {

    public CurrencyNotFoundException(Long id) {
        super("Currency", "id", id);
    }

    public CurrencyNotFoundException(String code) {
        super("Currency", "code", code);
    }

    public CurrencyNotFoundException(String fieldName, String fieldValue) {
        super("Currency", fieldName, fieldValue);
    }
}
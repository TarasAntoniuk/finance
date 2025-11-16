package com.tarasantoniuk.finance.currency.exception;

import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a currency is not found
 */
public class CurrencyNotFoundException extends ResourceNotFoundException {

    public CurrencyNotFoundException(Long id) {
        super("Currency", "id", id);
    }

    public CurrencyNotFoundException(String fieldName, String fieldValue) {
        super("Currency", fieldName, fieldValue);
    }

    // Статичні фабричні методи
    public static CurrencyNotFoundException byId(Long id) {
        return new CurrencyNotFoundException(id);
    }

    public static CurrencyNotFoundException byCode(String code) {
        return new CurrencyNotFoundException("code", code);
    }

    public static CurrencyNotFoundException byNumericCode(String numericCode) {
        return new CurrencyNotFoundException("numericCode", numericCode);
    }
}
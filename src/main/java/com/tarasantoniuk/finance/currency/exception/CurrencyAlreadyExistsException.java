package com.tarasantoniuk.finance.currency.exception;

import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;

/**
 * Exception thrown when a currency already exists
 */
public class CurrencyAlreadyExistsException extends ResourceAlreadyExistsException {

    public CurrencyAlreadyExistsException(String fieldName, String fieldValue) {
        super("Currency", fieldName, fieldValue);
    }

    // Статичні фабричні методи
    public static CurrencyAlreadyExistsException byCode(String code) {
        return new CurrencyAlreadyExistsException("code", code);
    }

    public static CurrencyAlreadyExistsException byNumericCode(String numericCode) {
        return new CurrencyAlreadyExistsException("numericCode", numericCode);
    }
}
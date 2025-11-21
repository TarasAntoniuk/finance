package com.tarasantoniuk.finance.core.country.exception;

import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;


/**
 * Exception thrown when a country already exists
 */
public class CountryAlreadyExistsException extends ResourceAlreadyExistsException {

    public CountryAlreadyExistsException(String fieldName, String fieldValue) {
        super("Country", fieldName, fieldValue);
    }

    // Статичні фабричні методи
    public static CountryAlreadyExistsException byIsoCode(String isoCode) {
        return new CountryAlreadyExistsException("isoCode", isoCode);
    }

    public static CountryAlreadyExistsException byName(String name) {
        return new CountryAlreadyExistsException("name", name);
    }
}
package com.tarasantoniuk.finance.country.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceNotFoundException;

/**
 * Exception thrown when a country is not found
 */
/**
 * Exception thrown when a country is not found
 */
public class CountryNotFoundException extends ResourceNotFoundException {

    public CountryNotFoundException(Long id) {
        super("Country", "id", id);
    }

    public CountryNotFoundException(String fieldName, String fieldValue) {
        super("Country", fieldName, fieldValue);
    }

    // Статичні фабричні методи для зручності використання
    public static CountryNotFoundException byId(Long id) {
        return new CountryNotFoundException(id);
    }

    public static CountryNotFoundException byIsoCode(String isoCode) {
        return new CountryNotFoundException("isoCode", isoCode);
    }

    public static CountryNotFoundException byName(String name) {
        return new CountryNotFoundException("name", name);
    }
}
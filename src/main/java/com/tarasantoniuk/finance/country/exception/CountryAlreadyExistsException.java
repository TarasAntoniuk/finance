package com.tarasantoniuk.finance.country.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceAlreadyExistsException;

/**
        * Exception thrown when a country already exists
 */
class CountryAlreadyExistsException extends ResourceAlreadyExistsException {

    public CountryAlreadyExistsException(String isoCode) {
        super("Country", "isoCode", isoCode);
    }

    public CountryAlreadyExistsException(String fieldName, String fieldValue) {
        super("Country", fieldName, fieldValue);
    }
}
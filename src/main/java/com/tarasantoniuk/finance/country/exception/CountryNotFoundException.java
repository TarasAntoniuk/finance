package com.tarasantoniuk.finance.country.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceNotFoundException;

/**
 * Exception thrown when a country is not found
 */
public class CountryNotFoundException extends ResourceNotFoundException {

    public CountryNotFoundException(Long id) {
        super("Country", "id", id);
    }

    public CountryNotFoundException(String isoCode) {
        super("Country", "isoCode", isoCode);
    }

//    public CountryNotFoundException(String message) {
//        super(message);
//    }
}
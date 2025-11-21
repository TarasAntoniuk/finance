package com.tarasantoniuk.finance.core.organization.exception;

import com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException;

/**
 * Exception thrown when an organization already exists
 */
public class OrganizationAlreadyExistsException extends ResourceAlreadyExistsException {

    public OrganizationAlreadyExistsException(String fieldName, String fieldValue) {
        super("Organization", fieldName, fieldValue);
    }

    // Статичні фабричні методи
    public static OrganizationAlreadyExistsException byRegistrationNumber(String registrationNumber) {
        return new OrganizationAlreadyExistsException("registrationNumber", registrationNumber);
    }

    public static OrganizationAlreadyExistsException byVatNumber(String vatNumber) {
        return new OrganizationAlreadyExistsException("vatNumber", vatNumber);
    }
}
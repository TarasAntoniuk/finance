package com.tarasantoniuk.finance.core.organization.exception;

import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when an organization is not found
 */
public class OrganizationNotFoundException extends ResourceNotFoundException {

    public OrganizationNotFoundException(Long id) {
        super("Organization", "id", id);
    }

    public OrganizationNotFoundException(String fieldName, String fieldValue) {
        super("Organization", fieldName, fieldValue);
    }

    // Статичні фабричні методи для зручності
    public static OrganizationNotFoundException byId(Long id) {
        return new OrganizationNotFoundException(id);
    }

    public static OrganizationNotFoundException byRegistrationNumber(String registrationNumber) {
        return new OrganizationNotFoundException("registrationNumber", registrationNumber);
    }

    public static OrganizationNotFoundException byName(String name) {
        return new OrganizationNotFoundException("name", name);
    }
}
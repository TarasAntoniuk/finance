package com.tarasantoniuk.finance.organization.exeption;

import com.tarasantoniuk.finance.common.exeption.ResourceAlreadyExistsException;

/**
 * Exception thrown when an organization already exists
 */
class OrganizationAlreadyExistsException extends ResourceAlreadyExistsException {

    public OrganizationAlreadyExistsException(String registrationNumber) {
        super("Organization", "registrationNumber", registrationNumber);
    }

    public OrganizationAlreadyExistsException(String fieldName, String fieldValue) {
        super("Organization", fieldName, fieldValue);
    }
}
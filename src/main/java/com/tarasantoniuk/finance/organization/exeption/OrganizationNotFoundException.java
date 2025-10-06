package com.tarasantoniuk.finance.organization.exeption;

import com.tarasantoniuk.finance.common.exeption.ResourceNotFoundException;

public class OrganizationNotFoundException extends ResourceNotFoundException {

    public OrganizationNotFoundException(Long id) {
        super("Organization", "id", id);
    }

    public OrganizationNotFoundException(String registrationNumber) {
        super("Organization", "registrationNumber", registrationNumber);
    }

//    public OrganizationNotFoundException(String message) {
//        super(message);
//    }
}
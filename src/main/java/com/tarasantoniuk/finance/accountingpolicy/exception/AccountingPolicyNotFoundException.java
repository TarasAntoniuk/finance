package com.tarasantoniuk.finance.accountingpolicy.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceNotFoundException;

/**
 * Exception thrown when an accounting policy is not found
 */
public class AccountingPolicyNotFoundException extends ResourceNotFoundException {

    public AccountingPolicyNotFoundException(Long id) {
        super("AccountingPolicy", "id", id);
    }

    public AccountingPolicyNotFoundException(String message) {
        super(message);
    }

    // Статичні фабричні методи
    public static AccountingPolicyNotFoundException byId(Long id) {
        return new AccountingPolicyNotFoundException(id);
    }

    public static AccountingPolicyNotFoundException byOrganizationAndYear(Long organizationId, Integer year) {
        return new AccountingPolicyNotFoundException(
                String.format("AccountingPolicy not found for organization %d and year %d", organizationId, year)
        );
    }
}
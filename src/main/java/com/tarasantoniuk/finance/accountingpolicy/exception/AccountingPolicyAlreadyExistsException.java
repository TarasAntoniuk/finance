package com.tarasantoniuk.finance.accountingpolicy.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceAlreadyExistsException;

/**
 * Exception thrown when an accounting policy already exists
 */
public class AccountingPolicyAlreadyExistsException extends ResourceAlreadyExistsException {

    public AccountingPolicyAlreadyExistsException(String message) {
        super(message);
    }

    // Статичний фабричний метод
    public static AccountingPolicyAlreadyExistsException forOrganizationAndYear(Long organizationId, Integer year) {
        return new AccountingPolicyAlreadyExistsException(
                String.format("AccountingPolicy already exists for organization %d and year %d", organizationId, year)
        );
    }
}
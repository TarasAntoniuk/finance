package com.tarasantoniuk.finance.core.counterparty.exception;

import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a counterparty is not found
 */
public class CounterpartyNotFoundException extends ResourceNotFoundException {

    public CounterpartyNotFoundException(Long id) {
        super("Counterparty", "id", id);
    }

    public CounterpartyNotFoundException(String message) {
        super(message);
    }

    // Static factory methods
    public static CounterpartyNotFoundException byId(Long id) {
        return new CounterpartyNotFoundException(id);
    }

    public static CounterpartyNotFoundException byCode(String code) {
        return new CounterpartyNotFoundException(
                String.format("Counterparty not found with code: %s", code)
        );
    }
}
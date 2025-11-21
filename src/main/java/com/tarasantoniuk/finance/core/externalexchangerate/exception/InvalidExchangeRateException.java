package com.tarasantoniuk.finance.core.externalexchangerate.exception;

import com.tarasantoniuk.finance.common.exception.InvalidOperationException;

/**
 * Exception thrown when invalid exchange rate operation is attempted
 */
public class InvalidExchangeRateException extends InvalidOperationException {

    public InvalidExchangeRateException(String message) {
        super(message);
    }

    public static InvalidExchangeRateException sameCurrency() {
        return new InvalidExchangeRateException("Currency From and Currency To cannot be the same");
    }

    public static InvalidExchangeRateException invalidRate() {
        return new InvalidExchangeRateException("Exchange rate must be greater than zero");
    }
}
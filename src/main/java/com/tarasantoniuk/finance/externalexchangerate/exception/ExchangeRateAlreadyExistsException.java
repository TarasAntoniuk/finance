package com.tarasantoniuk.finance.externalexchangerate.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceAlreadyExistsException;

import java.time.LocalDate;

/**
 * Exception thrown when an exchange rate already exists
 */
public class ExchangeRateAlreadyExistsException extends ResourceAlreadyExistsException {

    public ExchangeRateAlreadyExistsException(String message) {
        super(message);
    }

    // Статичний фабричний метод
    public static ExchangeRateAlreadyExistsException forDateCurrencyAndSource(
            LocalDate date, Long currencyFromId, Long currencyToId, String source) {
        return new ExchangeRateAlreadyExistsException(
                String.format("ExchangeRate already exists for date %s, currencyFrom %d, currencyTo %d, source %s",
                        date, currencyFromId, currencyToId, source)
        );
    }
}
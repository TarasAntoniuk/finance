package com.tarasantoniuk.finance.externalexchangerate.exception;

import com.tarasantoniuk.finance.common.exeption.ResourceNotFoundException;

import java.time.LocalDate;

/**
 * Exception thrown when an exchange rate is not found
 */
public class ExchangeRateNotFoundException extends ResourceNotFoundException {

    public ExchangeRateNotFoundException(Long id) {
        super("ExchangeRate", "id", id);
    }

    public ExchangeRateNotFoundException(String message) {
        super(message);
    }

    // Статичні фабричні методи
    public static ExchangeRateNotFoundException byId(Long id) {
        return new ExchangeRateNotFoundException(id);
    }

    public static ExchangeRateNotFoundException byDateAndCurrencyPair(LocalDate date, Long currencyFromId, Long currencyToId) {
        return new ExchangeRateNotFoundException(
                String.format("ExchangeRate not found for date %s, currencyFrom %d, currencyTo %d",
                        date, currencyFromId, currencyToId)
        );
    }

    public static ExchangeRateNotFoundException noPairFound(Long currencyFromId, Long currencyToId) {
        return new ExchangeRateNotFoundException(
                String.format("No exchange rates found for currency pair: %d -> %d", currencyFromId, currencyToId)
        );
    }
}

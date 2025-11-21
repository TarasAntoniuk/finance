package com.tarasantoniuk.finance.core.externalexchangerate.exception;

import com.tarasantoniuk.finance.common.exception.InvalidOperationException;

/**
 * Exception thrown when cross rate calculation fails
 */
public class CrossRateCalculationException extends InvalidOperationException {

    public CrossRateCalculationException(String message) {
        super(message);
    }

    public CrossRateCalculationException(Long currencyFromId, Long currencyToId,
                                         Long intermediateCurrencyId) {
        super(String.format("Cannot calculate cross rate from currency %d to currency %d " +
                        "via intermediate currency %d",
                currencyFromId, currencyToId, intermediateCurrencyId));
    }
}
package com.tarasantoniuk.finance.core.externalexchangerate.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for external exchange rate creation and update requests
 */
public class ExternalExchangeRateRequestDto extends BaseExternalExchangeRateDto {

    @NotNull(message = "Currency From ID is required")
    private Long currencyFromId;

    @NotNull(message = "Currency To ID is required")
    private Long currencyToId;

    public ExternalExchangeRateRequestDto() {
        super();
    }

    public ExternalExchangeRateRequestDto(LocalDate exchangeDate, Long currencyFromId,
                                          Long currencyToId, BigDecimal rate, String source,
                                          String sourceUrl, Boolean isActive) {
        super();
        setExchangeDate(exchangeDate);
        this.currencyFromId = currencyFromId;
        this.currencyToId = currencyToId;
        setRate(rate);
        setSource(source);
        setSourceUrl(sourceUrl);
        setIsActive(isActive);
    }

    public Long getCurrencyFromId() {
        return currencyFromId;
    }

    public void setCurrencyFromId(Long currencyFromId) {
        this.currencyFromId = currencyFromId;
    }

    public Long getCurrencyToId() {
        return currencyToId;
    }

    public void setCurrencyToId(Long currencyToId) {
        this.currencyToId = currencyToId;
    }
}
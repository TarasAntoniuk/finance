package com.tarasantoniuk.finance.core.externalexchangerate.dto;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;

import java.time.LocalDateTime;

/**
 * DTO for external exchange rate responses
 */
public class ExternalExchangeRateResponseDto extends BaseExternalExchangeRateDto {

    private Long id;
    private CurrencyResponseDto currencyFrom;
    private CurrencyResponseDto currencyTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExternalExchangeRateResponseDto() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CurrencyResponseDto getCurrencyFrom() {
        return currencyFrom;
    }

    public void setCurrencyFrom(CurrencyResponseDto currencyFrom) {
        this.currencyFrom = currencyFrom;
    }

    public CurrencyResponseDto getCurrencyTo() {
        return currencyTo;
    }

    public void setCurrencyTo(CurrencyResponseDto currencyTo) {
        this.currencyTo = currencyTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
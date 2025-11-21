package com.tarasantoniuk.finance.core.externalexchangerate.dto;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDTO;

import java.time.LocalDateTime;

/**
 * DTO for external exchange rate responses
 */
public class ExternalExchangeRateResponseDTO extends BaseExternalExchangeRateDto {

    private Long id;
    private CurrencyResponseDTO currencyFrom;
    private CurrencyResponseDTO currencyTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExternalExchangeRateResponseDTO() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CurrencyResponseDTO getCurrencyFrom() {
        return currencyFrom;
    }

    public void setCurrencyFrom(CurrencyResponseDTO currencyFrom) {
        this.currencyFrom = currencyFrom;
    }

    public CurrencyResponseDTO getCurrencyTo() {
        return currencyTo;
    }

    public void setCurrencyTo(CurrencyResponseDTO currencyTo) {
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
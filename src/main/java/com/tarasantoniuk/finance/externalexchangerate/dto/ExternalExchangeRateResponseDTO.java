package com.tarasantoniuk.finance.externalexchangerate.dto;

import com.tarasantoniuk.finance.currency.dto.CurrencyResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExternalExchangeRateResponseDTO {

    private Long id;
    private LocalDate exchangeDate;
    private CurrencyResponseDTO currencyFrom;
    private CurrencyResponseDTO currencyTo;
    private BigDecimal rate;
    private String source;
    private String sourceUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExternalExchangeRateResponseDTO() {
    }

    public ExternalExchangeRateResponseDTO(Long id, LocalDate exchangeDate,
                                           CurrencyResponseDTO currencyFrom,
                                           CurrencyResponseDTO currencyTo,
                                           BigDecimal rate, String source, String sourceUrl,
                                           Boolean isActive, LocalDateTime createdAt,
                                           LocalDateTime updatedAt) {
        this.id = id;
        this.exchangeDate = exchangeDate;
        this.currencyFrom = currencyFrom;
        this.currencyTo = currencyTo;
        this.rate = rate;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(LocalDate exchangeDate) {
        this.exchangeDate = exchangeDate;
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

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
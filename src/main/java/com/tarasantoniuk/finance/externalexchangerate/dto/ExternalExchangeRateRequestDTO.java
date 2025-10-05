package com.tarasantoniuk.finance.externalexchangerate.dto;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExternalExchangeRateRequestDTO {

    @NotNull(message = "Exchange date is required")
    private LocalDate exchangeDate;

    @NotNull(message = "Currency From ID is required")
    private Long currencyFromId;

    @NotNull(message = "Currency To ID is required")
    private Long currencyToId;

    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.000001", message = "Rate must be greater than 0")
    private BigDecimal rate;

    @NotBlank(message = "Source is required")
    @Size(max = 100, message = "Source must not exceed 100 characters")
    private String source;

    @Size(max = 500, message = "Source URL must not exceed 500 characters")
    private String sourceUrl;

    private Boolean isActive;

    public ExternalExchangeRateRequestDTO() {
    }

    public ExternalExchangeRateRequestDTO(LocalDate exchangeDate, Long currencyFromId,
                                          Long currencyToId, BigDecimal rate, String source,
                                          String sourceUrl, Boolean isActive) {
        this.exchangeDate = exchangeDate;
        this.currencyFromId = currencyFromId;
        this.currencyToId = currencyToId;
        this.rate = rate;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.isActive = isActive;
    }

    public LocalDate getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(LocalDate exchangeDate) {
        this.exchangeDate = exchangeDate;
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
}
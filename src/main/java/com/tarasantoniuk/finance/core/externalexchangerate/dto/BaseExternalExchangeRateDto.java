package com.tarasantoniuk.finance.core.externalexchangerate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Base DTO class with common fields for ExternalExchangeRate request and response DTOs
 */
public abstract class BaseExternalExchangeRateDto {

    @NotNull(message = "Exchange date is required")
    private LocalDate exchangeDate;

    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.000001", message = "Rate must be greater than 0")
    private BigDecimal rate;

    @NotBlank(message = "Source is required")
    @Size(max = 100, message = "Source must not exceed 100 characters")
    private String source;

    @Size(max = 500, message = "Source URL must not exceed 500 characters")
    private String sourceUrl;

    private Boolean isActive;

    public BaseExternalExchangeRateDto() {
    }

    public LocalDate getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(LocalDate exchangeDate) {
        this.exchangeDate = exchangeDate;
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
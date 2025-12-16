package com.tarasantoniuk.finance.core.country.dto;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;

import java.time.LocalDateTime;

/**
 * DTO for country responses
 */
public class CountryResponseDto extends BaseCountryDto {

    private Long id;
    private CurrencyResponseDto currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CountryResponseDto() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CurrencyResponseDto getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyResponseDto currency) {
        this.currency = currency;
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
package com.tarasantoniuk.finance.country.dto;

import com.tarasantoniuk.finance.currency.dto.CurrencyResponseDTO;

import java.time.LocalDateTime;

/**
 * DTO for country responses
 */
public class CountryResponseDTO extends BaseCountryDto {

    private Long id;
    private CurrencyResponseDTO currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CountryResponseDTO() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CurrencyResponseDTO getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyResponseDTO currency) {
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
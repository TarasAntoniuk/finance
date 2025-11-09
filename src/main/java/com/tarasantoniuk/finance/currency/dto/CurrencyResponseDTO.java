package com.tarasantoniuk.finance.currency.dto;

import java.time.LocalDateTime;

/**
 * DTO for currency responses
 */
public class CurrencyResponseDTO extends BaseCurrencyDto {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CurrencyResponseDTO() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
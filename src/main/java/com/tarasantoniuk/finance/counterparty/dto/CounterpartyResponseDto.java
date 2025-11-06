package com.tarasantoniuk.finance.counterparty.dto;



import com.tarasantoniuk.finance.country.dto.CountryResponseDTO;

import java.time.LocalDateTime;

public class CounterpartyResponseDto extends BaseCounterpartyDto {

    private Long id;
    private CountryResponseDTO country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CounterpartyResponseDto() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CountryResponseDTO getCountry() {
        return country;
    }

    public void setCountry(CountryResponseDTO country) {
        this.country = country;
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
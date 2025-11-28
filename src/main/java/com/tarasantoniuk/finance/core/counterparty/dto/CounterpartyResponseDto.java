package com.tarasantoniuk.finance.core.counterparty.dto;



import com.tarasantoniuk.finance.core.country.dto.CountryResponseDto;

import java.time.LocalDateTime;

public class CounterpartyResponseDto extends BaseCounterpartyDto {

    private Long id;
    private CountryResponseDto country;
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

    public CountryResponseDto getCountry() {
        return country;
    }

    public void setCountry(CountryResponseDto country) {
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
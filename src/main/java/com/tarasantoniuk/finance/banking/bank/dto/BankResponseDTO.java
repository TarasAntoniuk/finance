package com.tarasantoniuk.finance.banking.bank.dto;

import com.tarasantoniuk.finance.core.country.dto.CountryResponseDTO;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;

import java.time.LocalDateTime;

public class BankResponseDTO extends BaseBankDTO {

    private Long id;
    private CountryResponseDTO country;
    private CounterpartyResponseDto counterparty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BankResponseDTO() {
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

    public CounterpartyResponseDto getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartyResponseDto counterparty) {
        this.counterparty = counterparty;
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
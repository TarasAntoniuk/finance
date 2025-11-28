package com.tarasantoniuk.finance.banking.bank.dto;

import com.tarasantoniuk.finance.core.country.dto.CountryResponseDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;

import java.time.LocalDateTime;

public class BankResponseDto extends BaseBankDto {

    private Long id;
    private CountryResponseDto country;
    private CounterpartyResponseDto counterparty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BankResponseDto() {
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
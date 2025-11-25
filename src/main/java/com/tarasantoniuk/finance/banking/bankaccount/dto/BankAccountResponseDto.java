package com.tarasantoniuk.finance.banking.bankaccount.dto;

import com.tarasantoniuk.finance.banking.bank.dto.BankResponseDto;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;

import java.time.LocalDateTime;

public class BankAccountResponseDto extends BaseBankAccountDto {

    private Long id;
    private BankResponseDto bank;
    private CurrencyResponseDto currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BankAccountResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BankResponseDto getBank() {
        return bank;
    }

    public void setBank(BankResponseDto bank) {
        this.bank = bank;
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
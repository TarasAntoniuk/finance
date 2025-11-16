package com.tarasantoniuk.finance.bankaccount.dto;

import com.tarasantoniuk.finance.bank.dto.BankResponseDTO;
import com.tarasantoniuk.finance.currency.dto.CurrencyResponseDTO;

import java.time.LocalDateTime;

public class BankAccountResponseDTO extends BaseBankAccountDTO {

    private Long id;
    private BankResponseDTO bank;
    private CurrencyResponseDTO currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BankAccountResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BankResponseDTO getBank() {
        return bank;
    }

    public void setBank(BankResponseDTO bank) {
        this.bank = bank;
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
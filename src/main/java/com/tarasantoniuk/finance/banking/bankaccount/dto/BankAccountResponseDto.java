package com.tarasantoniuk.finance.banking.bankaccount.dto;

import com.tarasantoniuk.finance.banking.bank.dto.BankResponseDto;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Bank account response with full details and related entities")
public class BankAccountResponseDto extends BaseBankAccountDto {

    @Schema(description = "Unique identifier of the bank account", example = "1")
    private Long id;

    @Schema(description = "Bank information")
    private BankResponseDto bank;

    @Schema(description = "Currency information")
    private CurrencyResponseDto currency;

    @Schema(description = "Timestamp when the record was created", example = "2025-12-02T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the record was last updated", example = "2025-12-02T10:35:00")
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
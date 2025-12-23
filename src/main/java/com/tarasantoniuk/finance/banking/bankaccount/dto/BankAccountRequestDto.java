package com.tarasantoniuk.finance.banking.bankaccount.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request data for creating or updating a bank account")
public class BankAccountRequestDto extends BaseBankAccountDto {

    @Schema(
            description = "ID of the bank where the account is held",
            example = "1",
            required = true
    )
    @NotNull(message = "Bank ID is required")
    private Long bankId;

    @Schema(
            description = "ID of the account currency",
            example = "2",
            required = true
    )
    @NotNull(message = "Currency ID is required")
    private Long currencyId;

    public BankAccountRequestDto() {
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }
}
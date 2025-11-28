package com.tarasantoniuk.finance.banking.bankaccount.dto;

import jakarta.validation.constraints.NotNull;

public class BankAccountRequestDto extends BaseBankAccountDto {

    @NotNull(message = "Bank ID is required")
    private Long bankId;

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
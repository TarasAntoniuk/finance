package com.tarasantoniuk.finance.bankaccount.dto;

import jakarta.validation.constraints.NotNull;

public class BankAccountRequestDTO extends BaseBankAccountDTO {

    @NotNull(message = "Bank ID is required")
    private Long bankId;

    @NotNull(message = "Currency ID is required")
    private Long currencyId;

    public BankAccountRequestDTO() {
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
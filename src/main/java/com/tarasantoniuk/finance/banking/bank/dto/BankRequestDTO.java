package com.tarasantoniuk.finance.banking.bank.dto;

import jakarta.validation.constraints.NotNull;

public class BankRequestDTO extends BaseBankDTO {

    @NotNull(message = "Country ID is required")
    private Long countryId;

    private Long counterpartyId;

    public BankRequestDTO() {
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Long getCounterpartyId() {
        return counterpartyId;
    }

    public void setCounterpartyId(Long counterpartyId) {
        this.counterpartyId = counterpartyId;
    }
}
package com.tarasantoniuk.finance.banking.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request data for creating or updating a bank")
public class BankRequestDto extends BaseBankDto {

    @Schema(
            description = "ID of the country where the bank is located",
            example = "1",
            required = true
    )
    @NotNull(message = "Country ID is required")
    private Long countryId;

    @Schema(
            description = "ID of the counterparty associated with this bank (optional)",
            example = "5"
    )
    private Long counterpartyId;

    public BankRequestDto() {
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
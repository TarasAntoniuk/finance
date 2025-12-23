package com.tarasantoniuk.finance.banking.bankreceipt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating and updating bank receipts
 */
@Schema(description = "Request data for creating or updating a bank receipt")
public class BankReceiptRequestDto extends BaseBankReceiptDto {

    @Schema(
            description = "ID of the bank account receiving the funds",
            example = "1",
            required = true
    )
    @NotNull(message = "Account ID is required")
    private Long accountId;

    @Schema(
            description = "ID of the counterparty (payer)",
            example = "7",
            required = true
    )
    @NotNull(message = "Counterparty ID is required")
    private Long counterpartyId;

    @Schema(
            description = "ID of the counterparty's bank account (optional)",
            example = "6"
    )
    private Long counterpartyBankAccountId;

    @Schema(
            description = "ID of the currency for the transaction",
            example = "2",
            required = true
    )
    @NotNull(message = "Currency ID is required")
    private Long currencyId;

    @Schema(
            description = "ID of the organization receiving the funds",
            example = "1",
            required = true
    )
    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    // Getters and Setters

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCounterpartyId() {
        return counterpartyId;
    }

    public void setCounterpartyId(Long counterpartyId) {
        this.counterpartyId = counterpartyId;
    }

    public Long getCounterpartyBankAccountId() {
        return counterpartyBankAccountId;
    }

    public void setCounterpartyBankAccountId(Long counterpartyBankAccountId) {
        this.counterpartyBankAccountId = counterpartyBankAccountId;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}
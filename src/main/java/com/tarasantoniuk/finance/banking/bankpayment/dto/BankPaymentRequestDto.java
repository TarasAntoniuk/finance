package com.tarasantoniuk.finance.banking.bankpayment.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating and updating bank payments
 */
public class BankPaymentRequestDto extends BaseBankPaymentDto {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Counterparty ID is required")
    private Long counterpartyId;

    private Long counterpartyBankAccountId;

    @NotNull(message = "Currency ID is required")
    private Long currencyId;

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
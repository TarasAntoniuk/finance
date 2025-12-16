package com.tarasantoniuk.finance.banking.bankreceipt.dto;


import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountResponseDto;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDto;

import java.time.LocalDateTime;

/**
 * DTO for bank receipt responses
 */
public class BankReceiptResponseDto extends BaseBankReceiptDto {

    private Long id;

    private BankAccountResponseDto account;

    private CounterpartyResponseDto counterparty;

    private BankAccountResponseDto counterpartyBankAccount;

    private CurrencyResponseDto currency;

    private OrganizationResponseDto organization;

    private DocumentStatus status;

    private LocalDateTime postedAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BankAccountResponseDto getAccount() {
        return account;
    }

    public void setAccount(BankAccountResponseDto account) {
        this.account = account;
    }

    public CounterpartyResponseDto getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartyResponseDto counterparty) {
        this.counterparty = counterparty;
    }

    public BankAccountResponseDto getCounterpartyBankAccount() {
        return counterpartyBankAccount;
    }

    public void setCounterpartyBankAccount(BankAccountResponseDto counterpartyBankAccount) {
        this.counterpartyBankAccount = counterpartyBankAccount;
    }

    public CurrencyResponseDto getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyResponseDto currency) {
        this.currency = currency;
    }

    public OrganizationResponseDto getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationResponseDto organization) {
        this.organization = organization;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
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
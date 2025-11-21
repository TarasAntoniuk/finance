package com.tarasantoniuk.finance.core.accountingpolicy.dto;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDTO;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDTO;

import java.time.LocalDateTime;

/**
 * DTO for accounting policy responses
 */
public class AccountingPolicyResponseDTO extends BaseAccountingPolicyDto {

    private Long id;
    private OrganizationResponseDTO organization;
    private CurrencyResponseDTO currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountingPolicyResponseDTO() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrganizationResponseDTO getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationResponseDTO organization) {
        this.organization = organization;
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
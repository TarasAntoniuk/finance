package com.tarasantoniuk.finance.core.accountingpolicy.dto;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDto;

import java.time.LocalDateTime;

/**
 * DTO for accounting policy responses
 */
public class AccountingPolicyResponseDto extends BaseAccountingPolicyDto {

    private Long id;
    private OrganizationResponseDto organization;
    private CurrencyResponseDto currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountingPolicyResponseDto() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrganizationResponseDto getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationResponseDto organization) {
        this.organization = organization;
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
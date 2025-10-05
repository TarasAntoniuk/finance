package com.tarasantoniuk.finance.accountingpolicy.dto;

import com.tarasantoniuk.finance.currency.dto.CurrencyResponseDTO;
import com.tarasantoniuk.finance.organization.dto.OrganizationResponseDTO;

import java.time.LocalDateTime;

public class AccountingPolicyResponseDTO {

    private Long id;
    private OrganizationResponseDTO organization;
    private Integer year;
    private CurrencyResponseDTO currency;
    private Integer fiscalYearStartMonth;
    private String depreciationMethod;
    private String inventoryValuationMethod;
    private String revenueRecognitionMethod;
    private String vatAccountingMethod;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public AccountingPolicyResponseDTO() {
    }

    public AccountingPolicyResponseDTO(Long id, OrganizationResponseDTO organization,
                                       Integer year, CurrencyResponseDTO currency,
                                       Integer fiscalYearStartMonth, String depreciationMethod,
                                       String inventoryValuationMethod, String revenueRecognitionMethod,
                                       String vatAccountingMethod, Boolean isActive, String notes,
                                       LocalDateTime createdAt, LocalDateTime updatedAt,
                                       String createdBy, String updatedBy) {
        this.id = id;
        this.organization = organization;
        this.year = year;
        this.currency = currency;
        this.fiscalYearStartMonth = fiscalYearStartMonth;
        this.depreciationMethod = depreciationMethod;
        this.inventoryValuationMethod = inventoryValuationMethod;
        this.revenueRecognitionMethod = revenueRecognitionMethod;
        this.vatAccountingMethod = vatAccountingMethod;
        this.isActive = isActive;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public CurrencyResponseDTO getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyResponseDTO currency) {
        this.currency = currency;
    }

    public Integer getFiscalYearStartMonth() {
        return fiscalYearStartMonth;
    }

    public void setFiscalYearStartMonth(Integer fiscalYearStartMonth) {
        this.fiscalYearStartMonth = fiscalYearStartMonth;
    }

    public String getDepreciationMethod() {
        return depreciationMethod;
    }

    public void setDepreciationMethod(String depreciationMethod) {
        this.depreciationMethod = depreciationMethod;
    }

    public String getInventoryValuationMethod() {
        return inventoryValuationMethod;
    }

    public void setInventoryValuationMethod(String inventoryValuationMethod) {
        this.inventoryValuationMethod = inventoryValuationMethod;
    }

    public String getRevenueRecognitionMethod() {
        return revenueRecognitionMethod;
    }

    public void setRevenueRecognitionMethod(String revenueRecognitionMethod) {
        this.revenueRecognitionMethod = revenueRecognitionMethod;
    }

    public String getVatAccountingMethod() {
        return vatAccountingMethod;
    }

    public void setVatAccountingMethod(String vatAccountingMethod) {
        this.vatAccountingMethod = vatAccountingMethod;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
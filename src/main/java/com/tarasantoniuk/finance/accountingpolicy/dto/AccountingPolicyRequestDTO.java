package com.tarasantoniuk.finance.accountingpolicy.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class AccountingPolicyRequestDTO {

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    @Max(value = 2100, message = "Year must not exceed 2100")
    private Integer year;

    @NotNull(message = "Currency ID is required")
    private Long currencyId;

    @Min(value = 1, message = "Fiscal year start month must be between 1 and 12")
    @Max(value = 12, message = "Fiscal year start month must be between 1 and 12")
    private Integer fiscalYearStartMonth;

    @Size(max = 50, message = "Depreciation method must not exceed 50 characters")
    private String depreciationMethod;

    @Size(max = 50, message = "Inventory valuation method must not exceed 50 characters")
    private String inventoryValuationMethod;

    @Size(max = 50, message = "Revenue recognition method must not exceed 50 characters")
    private String revenueRecognitionMethod;

    @Size(max = 50, message = "VAT accounting method must not exceed 50 characters")
    private String vatAccountingMethod;

    private Boolean isActive;

    private String notes;

    private String createdBy;

    private String updatedBy;

    public AccountingPolicyRequestDTO() {
    }

    public AccountingPolicyRequestDTO(Long organizationId, Integer year, Long currencyId,
                                      Integer fiscalYearStartMonth, String depreciationMethod,
                                      String inventoryValuationMethod, String revenueRecognitionMethod,
                                      String vatAccountingMethod, Boolean isActive, String notes,
                                      String createdBy, String updatedBy) {
        this.organizationId = organizationId;
        this.year = year;
        this.currencyId = currencyId;
        this.fiscalYearStartMonth = fiscalYearStartMonth;
        this.depreciationMethod = depreciationMethod;
        this.inventoryValuationMethod = inventoryValuationMethod;
        this.revenueRecognitionMethod = revenueRecognitionMethod;
        this.vatAccountingMethod = vatAccountingMethod;
        this.isActive = isActive;
        this.notes = notes;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
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
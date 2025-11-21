package com.tarasantoniuk.finance.core.accountingpolicy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Base DTO class with common fields for AccountingPolicy request and response DTOs
 */
public abstract class BaseAccountingPolicyDto {

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    @Max(value = 2100, message = "Year must not exceed 2100")
    private Integer year;

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

    public BaseAccountingPolicyDto() {
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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
}
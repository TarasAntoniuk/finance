package com.tarasantoniuk.finance.core.accountingpolicy.dto;

import com.tarasantoniuk.finance.core.accountingpolicy.enums.DepreciationMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.InventoryValuationMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.RevenueRecognitionMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.VatAccountingMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

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

    private DepreciationMethod depreciationMethod;

    private InventoryValuationMethod inventoryValuationMethod;

    private RevenueRecognitionMethod revenueRecognitionMethod;

    private VatAccountingMethod vatAccountingMethod;

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

    public DepreciationMethod getDepreciationMethod() {
        return depreciationMethod;
    }

    public void setDepreciationMethod(DepreciationMethod depreciationMethod) {
        this.depreciationMethod = depreciationMethod;
    }

    public InventoryValuationMethod getInventoryValuationMethod() {
        return inventoryValuationMethod;
    }

    public void setInventoryValuationMethod(InventoryValuationMethod inventoryValuationMethod) {
        this.inventoryValuationMethod = inventoryValuationMethod;
    }

    public RevenueRecognitionMethod getRevenueRecognitionMethod() {
        return revenueRecognitionMethod;
    }

    public void setRevenueRecognitionMethod(RevenueRecognitionMethod revenueRecognitionMethod) {
        this.revenueRecognitionMethod = revenueRecognitionMethod;
    }

    public VatAccountingMethod getVatAccountingMethod() {
        return vatAccountingMethod;
    }

    public void setVatAccountingMethod(VatAccountingMethod vatAccountingMethod) {
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
package com.tarasantoniuk.finance.core.accountingpolicy.dto;

import com.tarasantoniuk.finance.core.accountingpolicy.enums.DepreciationMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.InventoryValuationMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.RevenueRecognitionMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.VatAccountingMethod;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for accounting policy creation and update requests
 */
public class AccountingPolicyRequestDto extends BaseAccountingPolicyDto {

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotNull(message = "Currency ID is required")
    private Long currencyId;

    public AccountingPolicyRequestDto() {
        super();
    }

    public AccountingPolicyRequestDto(Long organizationId, Integer year, Long currencyId,
                                      Integer fiscalYearStartMonth, DepreciationMethod depreciationMethod,
                                      InventoryValuationMethod inventoryValuationMethod, RevenueRecognitionMethod revenueRecognitionMethod,
                                      VatAccountingMethod vatAccountingMethod, Boolean isActive, String notes) {
        super();
        this.organizationId = organizationId;
        setYear(year);
        this.currencyId = currencyId;
        setFiscalYearStartMonth(fiscalYearStartMonth);
        setDepreciationMethod(depreciationMethod);
        setInventoryValuationMethod(inventoryValuationMethod);
        setRevenueRecognitionMethod(revenueRecognitionMethod);
        setVatAccountingMethod(vatAccountingMethod);
        setIsActive(isActive);
        setNotes(notes);
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }
}
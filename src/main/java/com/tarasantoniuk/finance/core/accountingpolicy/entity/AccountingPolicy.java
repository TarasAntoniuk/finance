package com.tarasantoniuk.finance.core.accountingpolicy.entity;


import com.tarasantoniuk.finance.common.entity.BaseEntity;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.DepreciationMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.InventoryValuationMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.RevenueRecognitionMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.VatAccountingMethod;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "accounting_policies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "year"}))
public class AccountingPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "year", nullable = false)
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "fiscal_year_start_month")
    private Integer fiscalYearStartMonth; // 1-12, default 1 (January)

    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_method", length = 50)
    private DepreciationMethod depreciationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_valuation_method", length = 50)
    private InventoryValuationMethod inventoryValuationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "revenue_recognition_method", length = 50)
    private RevenueRecognitionMethod revenueRecognitionMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "vat_accounting_method", length = 50)
    private VatAccountingMethod vatAccountingMethod;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public AccountingPolicy() {
    }

    public AccountingPolicy(Long id, Organization organization, Integer year,
                            Currency currency, Integer fiscalYearStartMonth,
                            DepreciationMethod depreciationMethod, InventoryValuationMethod inventoryValuationMethod,
                            RevenueRecognitionMethod revenueRecognitionMethod, VatAccountingMethod vatAccountingMethod,
                            Boolean isActive, String notes) {
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

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountingPolicy that = (AccountingPolicy) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, year);
    }

    @Override
    public String toString() {
        return "AccountingPolicy{" +
                "id=" + id +
                ", year=" + year +
                ", fiscalYearStartMonth=" + fiscalYearStartMonth +
                ", isActive=" + isActive +
                '}';
    }
}
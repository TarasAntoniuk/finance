package com.tarasantoniuk.finance.core.accountingpolicy.entity;


import com.tarasantoniuk.finance.common.entity.BaseEntity;
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

    @Column(name = "depreciation_method", length = 50)
    private String depreciationMethod; // STRAIGHT_LINE, DECLINING_BALANCE, etc.

    @Column(name = "inventory_valuation_method", length = 50)
    private String inventoryValuationMethod; // FIFO, LIFO, WEIGHTED_AVERAGE

    @Column(name = "revenue_recognition_method", length = 50)
    private String revenueRecognitionMethod; // ACCRUAL, CASH

    @Column(name = "vat_accounting_method", length = 50)
    private String vatAccountingMethod; // INVOICE, PAYMENT

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public AccountingPolicy() {
    }

    public AccountingPolicy(Long id, Organization organization, Integer year,
                            Currency currency, Integer fiscalYearStartMonth,
                            String depreciationMethod, String inventoryValuationMethod,
                            String revenueRecognitionMethod, String vatAccountingMethod,
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountingPolicy that = (AccountingPolicy) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(organization, that.organization) &&
                Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, organization, year);
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
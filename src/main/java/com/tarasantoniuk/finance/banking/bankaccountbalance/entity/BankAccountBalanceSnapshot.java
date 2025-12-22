package com.tarasantoniuk.finance.banking.bankaccountbalance.entity;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bank account balance snapshot for a specific point in time.
 * Used to optimize balance calculation:
 * instead of recalculating all events from the beginning of time,
 * we take the last snapshot and add events after it.
 * <p>
 * Example:
 * - Snapshot for 2024-01-01 23:59:59: balance = 10000
 * - Events from 2024-01-02 00:00:00 to now: +5000
 * - Current balance = 10000 + 5000 = 15000
 */
@Entity
@Table(
        name = "bank_account_balance_snapshots",
        indexes = {
                @Index(name = "idx_bank_account_balance_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_bank_account_balance_datetime", columnList = "snapshot_date_time"),
                @Index(name = "idx_bank_account_balance_organization", columnList = "organization_id, snapshot_date_time")
        }
)
public class BankAccountBalanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bank account reference
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    /**
     * Organization that owns the account
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /**
     * Account currency
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    /**
     * Snapshot point in time
     */
    @NotNull
    @Column(name = "snapshot_date_time", nullable = false)
    private LocalDateTime snapshotDateTime;

    /**
     * Opening balance (balance at the beginning of the period)
     */
    @NotNull
    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance;

    /**
     * Debit turnover for the period (total receipts)
     */
    @NotNull
    @Column(name = "debit_turnover", nullable = false, precision = 19, scale = 4)
    private BigDecimal debitTurnover;

    /**
     * Credit turnover for the period (total payments)
     */
    @NotNull
    @Column(name = "credit_turnover", nullable = false, precision = 19, scale = 4)
    private BigDecimal creditTurnover;

    /**
     * Closing balance at snapshot point in time
     * closingBalance = openingBalance + debitTurnover - creditTurnover
     */
    @NotNull
    @Column(name = "closing_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal closingBalance;

    /**
     * ID of the last event included in this snapshot
     */
    @Column(name = "last_event_id")
    private Long lastEventId;

    /**
     * Number of events included in this snapshot
     */
    @Column(name = "events_count", nullable = false)
    private Integer eventsCount = 0;

    /**
     * Snapshot creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Snapshot last update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (eventsCount == null) {
            eventsCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public LocalDateTime getSnapshotDateTime() {
        return snapshotDateTime;
    }

    public void setSnapshotDateTime(LocalDateTime snapshotDateTime) {
        this.snapshotDateTime = snapshotDateTime;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getDebitTurnover() {
        return debitTurnover;
    }

    public void setDebitTurnover(BigDecimal debitTurnover) {
        this.debitTurnover = debitTurnover;
    }

    public BigDecimal getCreditTurnover() {
        return creditTurnover;
    }

    public void setCreditTurnover(BigDecimal creditTurnover) {
        this.creditTurnover = creditTurnover;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public Long getLastEventId() {
        return lastEventId;
    }

    public void setLastEventId(Long lastEventId) {
        this.lastEventId = lastEventId;
    }

    public Integer getEventsCount() {
        return eventsCount;
    }

    public void setEventsCount(Integer eventsCount) {
        this.eventsCount = eventsCount;
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

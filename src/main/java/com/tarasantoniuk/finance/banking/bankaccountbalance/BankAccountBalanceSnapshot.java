package com.tarasantoniuk.finance.banking.bankaccountbalance;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bank account balance snapshot for a specific date.
 * Used to optimize balance calculation:
 * instead of recalculating all events from the beginning of time,
 * we take the last snapshot and add events after it.
 * <p>
 * Example:
 * - Snapshot for 2024-01-01: balance = 10000
 * - Events from 2024-01-02 to 2024-01-15: +5000
 * - Current balance = 10000 + 5000 = 15000
 */
@Entity
@Table(
        name = "bank_account_balance_snapshots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bank_account_balance_snapshot",
                        columnNames = {"bank_account_id", "snapshot_date"}
                )
        },
        indexes = {
                @Index(name = "idx_bank_account_balance_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_bank_account_balance_date", columnList = "snapshot_date"),
                @Index(name = "idx_bank_account_balance_organization", columnList = "organization_id, snapshot_date")
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
     * Snapshot date
     */
    @NotNull
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    /**
     * Opening balance (balance at the beginning of the day)
     */
    @NotNull
    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance;

    /**
     * Debit turnover for the day (total receipts)
     */
    @NotNull
    @Column(name = "debit_turnover", nullable = false, precision = 19, scale = 4)
    private BigDecimal debitTurnover;

    /**
     * Credit turnover for the day (total payments)
     */
    @NotNull
    @Column(name = "credit_turnover", nullable = false, precision = 19, scale = 4)
    private BigDecimal creditTurnover;

    /**
     * Closing balance (balance at the end of the day)
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

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
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

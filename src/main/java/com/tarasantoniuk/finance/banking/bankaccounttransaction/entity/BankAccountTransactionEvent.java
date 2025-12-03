package com.tarasantoniuk.finance.banking.bankaccounttransaction.entity;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event Sourcing entity for storing all bank account transactions.
 * Each operation (bank receipt, bank payment) creates a separate event.
 * Events are immutable - this is the foundation of Event Sourcing.
 * <p>
 * Supports:
 * - Bank receipts (money in)
 * - Bank payments (money out)
 * - Transaction reversals
 */
@Entity
@Table(
        name = "bank_account_transaction_events",
        indexes = {
                @Index(name = "idx_bank_account_transaction_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_bank_account_transaction_document", columnList = "document_type, document_id"),
                @Index(name = "idx_bank_account_transaction_date", columnList = "transaction_date"),
                @Index(name = "idx_bank_account_transaction_created", columnList = "created_at")
        }
)
public class BankAccountTransactionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bank account where transaction occurred
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
     * Transaction currency
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    /**
     * Transaction date (for sorting and balance calculation)
     */
    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    /**
     * Transaction type (DEBIT - receipt, CREDIT - payment)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    /**
     * Transaction amount (always positive)
     */
    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /**
     * Source document type (BankReceipt, BankPayment, etc.)
     */
    @NotNull
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    /**
     * Source document ID
     */
    @NotNull
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    /**
     * Transaction description
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Balance after this transaction (denormalization for performance)
     */
    @Column(name = "balance_after", precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    /**
     * Whether this transaction has been reversed
     */
    @Column(name = "is_reversed", nullable = false)
    private Boolean isReversed = false;

    /**
     * ID of the event that reverses this transaction
     */
    @Column(name = "reversed_by_event_id")
    private Long reversedByEventId;

    /**
     * Event creation timestamp (important for Event Sourcing to know event order)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isReversed == null) {
            isReversed = false;
        }
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

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Boolean getIsReversed() {
        return isReversed;
    }

    public void setIsReversed(Boolean isReversed) {
        this.isReversed = isReversed;
    }

    public Long getReversedByEventId() {
        return reversedByEventId;
    }

    public void setReversedByEventId(Long reversedByEventId) {
        this.reversedByEventId = reversedByEventId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
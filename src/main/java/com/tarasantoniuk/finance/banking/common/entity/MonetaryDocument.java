package com.tarasantoniuk.finance.banking.common.entity;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.common.enums.TransactionType;
import com.tarasantoniuk.finance.common.document.entity.BaseDocument;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base class for all monetary documents (bank receipts, payments, cash operations).
 * Represents a single payment transaction from bank statement or manual entry.
 * <p>
 * One document can create multiple accounting events (main transaction + bank commission).
 * <p>
 * Document Lifecycle:
 * - DRAFT: mutable, can be edited or deleted
 * - POSTED: immutable, creates AccountTransactionEvent(s)
 * - CANCELLED: immutable, creates reversal AccountTransactionEvent(s)
 */
@MappedSuperclass
public abstract class MonetaryDocument extends BaseDocument {

    /**
     * Our bank account where transaction occurred
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount account;

    /**
     * Transaction amount (excluding bank commission)
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /**
     * Currency of the transaction
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    /**
     * Bank commission/fee for this transaction.
     * If not null, creates separate accounting event for commission.
     */
    @Column(precision = 19, scale = 4)
    private BigDecimal bankCommission;

    /**
     * Counterparty involved in the transaction
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id", nullable = false)
    private Counterparty counterparty;

    /**
     * Counterparty's bank account.
     * Auto-created during bank statement import if not exists.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_account_id")
    private BankAccount counterpartyBankAccount;

    /**
     * Payment purpose - detailed explanation from bank statement.
     * Can contain invoice numbers, contract references, etc.
     */
    @Column(length = 2000)
    private String paymentPurpose;

    /**
     * Reference to contract/invoice/order number
     */
    @Column(length = 100)
    private String paymentReference;

    /**
     * External transaction ID from bank (for idempotency).
     * Prevents duplicate imports from bank statements.
     * Must be unique across all monetary documents.
     */
    @Column(unique = true, length = 100)
    private String externalTransactionId;

    /**
     * Bank's internal reference number
     */
    @Column(length = 100)
    private String bankReference;

    /**
     * Date when transaction was booked in the bank.
     * Can differ from documentDate (when we received/created document).
     */
    @Column
    private LocalDate transactionDate;

    /**
     * Value date - when money is actually available.
     * For receipts: when money can be used.
     * For payments: when money is debited.
     */
    @Column
    private LocalDate valueDate;

    /**
     * When bank processed the transaction (with time).
     * Used for audit and reconciliation.
     */
    @Column
    private LocalDateTime bankProcessedAt;

    // Getters and Setters

    public BankAccount getAccount() {
        return account;
    }

    public void setAccount(BankAccount account) {
        this.account = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getBankCommission() {
        return bankCommission;
    }

    public void setBankCommission(BigDecimal bankCommission) {
        this.bankCommission = bankCommission;
    }

    public Counterparty getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(Counterparty counterparty) {
        this.counterparty = counterparty;
    }

    public BankAccount getCounterpartyBankAccount() {
        return counterpartyBankAccount;
    }

    public void setCounterpartyBankAccount(BankAccount counterpartyBankAccount) {
        this.counterpartyBankAccount = counterpartyBankAccount;
    }

    public String getPaymentPurpose() {
        return paymentPurpose;
    }

    public void setPaymentPurpose(String paymentPurpose) {
        this.paymentPurpose = paymentPurpose;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getBankReference() {
        return bankReference;
    }

    public void setBankReference(String bankReference) {
        this.bankReference = bankReference;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public LocalDateTime getBankProcessedAt() {
        return bankProcessedAt;
    }

    public void setBankProcessedAt(LocalDateTime bankProcessedAt) {
        this.bankProcessedAt = bankProcessedAt;
    }

    // Business methods

    /**
     * Returns the transaction type (INBOUND or OUTBOUND).
     * Must be implemented by concrete document classes (BankReceipt, BankPayment, etc.).
     *
     * @return TransactionType indicating money flow direction
     */
    public abstract TransactionType getTransactionType();

    /**
     * Calculates total amount including bank commission.
     * Used for balance calculations and reporting.
     *
     * @return total amount to be debited/credited from account
     */
    public BigDecimal getTotalAmount() {
        BigDecimal total = amount;
        if (bankCommission != null && bankCommission.compareTo(BigDecimal.ZERO) > 0) {
            total = total.add(bankCommission);
        }
        return total;
    }

    /**
     * Checks if document has bank commission.
     *
     * @return true if bank commission is present and greater than zero
     */
    public boolean hasBankCommission() {
        return bankCommission != null && bankCommission.compareTo(BigDecimal.ZERO) > 0;
    }
}
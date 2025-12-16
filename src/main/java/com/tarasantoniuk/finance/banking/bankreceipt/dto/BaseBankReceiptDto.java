package com.tarasantoniuk.finance.banking.bankreceipt.dto;

import com.tarasantoniuk.finance.banking.bankreceipt.enums.ReceiptType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base DTO containing common fields for bank receipt operations
 */
public abstract class BaseBankReceiptDto {

    @NotNull(message = "Document date is required")
    private LocalDate documentDate;

    @NotNull(message = "Receipt type is required")
    private ReceiptType receiptType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;


    private BigDecimal bankCommission;

    private String description;

    private String paymentPurpose;

    private String paymentReference;

    private String incomingDocumentNumber;

    private LocalDate incomingDocumentDate;

    private LocalDate transactionDate;

    private LocalDate valueDate;

    private LocalDateTime bankProcessedAt;

    private String externalTransactionId;

    private String bankReference;

    // Getters and Setters

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public ReceiptType getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(ReceiptType receiptType) {
        this.receiptType = receiptType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBankCommission() {
        return bankCommission;
    }

    public void setBankCommission(BigDecimal bankCommission) {
        this.bankCommission = bankCommission;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getIncomingDocumentNumber() {
        return incomingDocumentNumber;
    }

    public void setIncomingDocumentNumber(String incomingDocumentNumber) {
        this.incomingDocumentNumber = incomingDocumentNumber;
    }

    public LocalDate getIncomingDocumentDate() {
        return incomingDocumentDate;
    }

    public void setIncomingDocumentDate(LocalDate incomingDocumentDate) {
        this.incomingDocumentDate = incomingDocumentDate;
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
}
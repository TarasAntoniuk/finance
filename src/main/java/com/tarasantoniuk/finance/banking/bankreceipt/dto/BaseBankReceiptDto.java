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

    @NotNull(message = "Transaction date and time is required")
    private LocalDateTime transactionDateTime;

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

    private LocalDate valueDate;

    private String externalTransactionId;

    private String bankReference;

    // Getters and Setters

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(LocalDateTime transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
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

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
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

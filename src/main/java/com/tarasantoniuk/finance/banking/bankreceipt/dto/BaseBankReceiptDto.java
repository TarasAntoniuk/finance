package com.tarasantoniuk.finance.banking.bankreceipt.dto;

import com.tarasantoniuk.finance.banking.bankreceipt.enums.ReceiptType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base DTO containing common fields for bank receipt operations
 */
@Schema(description = "Base bank receipt information")
public abstract class BaseBankReceiptDto {

    @Schema(
            description = "Transaction date and time when the receipt occurred",
            example = "2025-12-02T10:30:00",
            required = true
    )
    @NotNull(message = "Transaction date and time is required")
    private LocalDateTime transactionDateTime;

    @Schema(
            description = "Type of receipt indicating the nature of the transaction",
            example = "CUSTOMER_PAYMENT",
            required = true,
            allowableValues = {"CUSTOMER_PAYMENT", "OTHER_INCOME", "LOAN_RECEIVED", "EQUITY_CONTRIBUTION"}
    )
    @NotNull(message = "Receipt type is required")
    private ReceiptType receiptType;

    @Schema(
            description = "Receipt amount in the specified currency",
            example = "10000.00",
            required = true,
            minimum = "0.01"
    )
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(
            description = "Bank commission charged for the transaction (if any)",
            example = "50.00",
            minimum = "0"
    )
    private BigDecimal bankCommission;

    @Schema(
            description = "General description of the receipt",
            example = "Payment for software development services"
    )
    private String description;

    @Schema(
            description = "Purpose of the payment as specified in the bank statement",
            example = "Invoice #INV-2025-125, December services"
    )
    private String paymentPurpose;

    @Schema(
            description = "Reference number for the payment (e.g., invoice number)",
            example = "INV-2025-125"
    )
    private String paymentReference;

    @Schema(
            description = "Document number from the incoming bank statement",
            example = "PAY-DEC-12345"
    )
    private String incomingDocumentNumber;

    @Schema(
            description = "Value date when the funds were actually credited",
            example = "2025-12-02"
    )
    private LocalDate valueDate;

    @Schema(
            description = "External transaction identifier from the bank",
            example = "BANK-TXN-2025-DEC-001"
    )
    private String externalTransactionId;

    @Schema(
            description = "Bank's reference number for the transaction",
            example = "BANK-REF-123456"
    )
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
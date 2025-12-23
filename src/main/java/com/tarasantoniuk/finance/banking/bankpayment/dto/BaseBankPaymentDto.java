package com.tarasantoniuk.finance.banking.bankpayment.dto;

import com.tarasantoniuk.finance.banking.bankpayment.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Base DTO containing common fields for bank payment operations
 */
@Schema(description = "Base bank payment information")
public abstract class BaseBankPaymentDto {

    @Schema(
            description = "Transaction date and time when the payment occurred",
            example = "2025-12-02T10:30:00",
            required = true
    )
    @NotNull(message = "Transaction date and time is required")
    private LocalDateTime transactionDateTime;

    @Schema(
            description = "Type of payment indicating the nature of the transaction",
            example = "SUPPLIER_PAYMENT",
            required = true,
            allowableValues = {"SUPPLIER_PAYMENT", "SALARY_PAYMENT", "TAX_PAYMENT", "LOAN_REPAYMENT", "OTHER_EXPENSE"}
    )
    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @Schema(
            description = "Payment amount in the specified currency",
            example = "5000.00",
            required = true,
            minimum = "0.01"
    )
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(
            description = "Bank commission charged for the transaction (if any)",
            example = "25.00",
            minimum = "0"
    )
    private BigDecimal bankCommission;

    @Schema(
            description = "General description of the payment",
            example = "Payment for consulting services"
    )
    private String description;

    @Schema(
            description = "Purpose of the payment as specified in the bank statement",
            example = "Invoice #SUP-2025-100, November services"
    )
    private String paymentPurpose;

    @Schema(
            description = "Reference number for the payment (e.g., invoice number)",
            example = "SUP-2025-100"
    )
    private String paymentReference;

    @Schema(
            description = "Document number for the outgoing payment order",
            example = "PAY-OUT-12345"
    )
    private String outgoingDocumentNumber;

    @Schema(
            description = "Value date when the funds were actually debited",
            example = "2025-12-02"
    )
    private LocalDate valueDate;

    @Schema(
            description = "External transaction identifier from the bank",
            example = "BANK-TXN-OUT-2025-001"
    )
    private String externalTransactionId;

    @Schema(
            description = "Bank's reference number for the transaction",
            example = "BANK-REF-654321"
    )
    private String bankReference;

    // Getters and Setters

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(LocalDateTime transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
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

    public String getOutgoingDocumentNumber() {
        return outgoingDocumentNumber;
    }

    public void setOutgoingDocumentNumber(String outgoingDocumentNumber) {
        this.outgoingDocumentNumber = outgoingDocumentNumber;
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

package com.tarasantoniuk.finance.banking.bankpayment.entity;

import com.tarasantoniuk.finance.banking.bankpayment.enums.PaymentType;
import com.tarasantoniuk.finance.banking.common.entity.MonetaryDocument;
import com.tarasantoniuk.finance.banking.common.enums.TransactionType;
import jakarta.persistence.*;

/**
 * Bank Payment - document representing money paid from bank account.
 * <p>
 * Examples:
 * - Payment to supplier for goods/services
 * - Salary payment to employee
 * - Tax payment
 * - Loan repayment
 * - Refund to customer
 * <p>
 * When posted, creates OUTBOUND transaction event(s).
 */
@Entity
@Table(name = "bank_payments", indexes = {
        @Index(name = "idx_bank_payment_account", columnList = "account_id"),
        @Index(name = "idx_bank_payment_counterparty", columnList = "counterparty_id"),
        @Index(name = "idx_bank_payment_external_id", columnList = "externalTransactionId"),
        @Index(name = "idx_bank_payment_status", columnList = "status"),
        @Index(name = "idx_bank_payment_doc_date", columnList = "documentDate")
})
public class BankPayment extends MonetaryDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_payment_seq")
    @SequenceGenerator(
            name = "bank_payment_seq",
            sequenceName = "bank_payment_id_seq",
            allocationSize = 50
    )
    private Long id;

    /**
     * Type of payment transaction
     */
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    /**
     * Outgoing document number (payment order number, contract reference, etc.)
     */
    @Column(length = 100)
    private String outgoingDocumentNumber;

    /**
     * Date of outgoing document
     */
    @Column
    private java.time.LocalDate outgoingDocumentDate;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getOutgoingDocumentNumber() {
        return outgoingDocumentNumber;
    }

    public void setOutgoingDocumentNumber(String outgoingDocumentNumber) {
        this.outgoingDocumentNumber = outgoingDocumentNumber;
    }

    public java.time.LocalDate getOutgoingDocumentDate() {
        return outgoingDocumentDate;
    }

    public void setOutgoingDocumentDate(java.time.LocalDate outgoingDocumentDate) {
        this.outgoingDocumentDate = outgoingDocumentDate;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.OUTBOUND;
    }
}
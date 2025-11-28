package com.tarasantoniuk.finance.banking.bankreceipt.entity;

import com.tarasantoniuk.finance.banking.bankreceipt.enums.ReceiptType;
import com.tarasantoniuk.finance.banking.common.entity.MonetaryDocument;
import com.tarasantoniuk.finance.banking.common.enums.TransactionType;
import jakarta.persistence.*;

/**
 * Bank Receipt - document representing money received into bank account.
 *
 * Examples:
 * - Customer payment for goods/services
 * - Loan received from bank
 * - Refund from supplier
 * - Interest income
 *
 * When posted, creates INBOUND transaction event(s).
 */
@Entity
@Table(name = "bank_receipts", indexes = {
        @Index(name = "idx_bank_receipt_account", columnList = "account_id"),
        @Index(name = "idx_bank_receipt_counterparty", columnList = "counterparty_id"),
        @Index(name = "idx_bank_receipt_external_id", columnList = "externalTransactionId"),
        @Index(name = "idx_bank_receipt_status", columnList = "status"),
        @Index(name = "idx_bank_receipt_doc_date", columnList = "documentDate")
})
public class BankReceipt extends MonetaryDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_receipt_seq")
    @SequenceGenerator(
            name = "bank_receipt_seq",
            sequenceName = "bank_receipt_id_seq",
            allocationSize = 50
    )
    private Long id;

    /**
     * Type of receipt transaction
     */
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ReceiptType receiptType;

    /**
     * Incoming document number from counterparty (invoice, contract, etc.)
     */
    @Column(length = 100)
    private String incomingDocumentNumber;

    /**
     * Date of incoming document from counterparty
     */
    @Column
    private java.time.LocalDate incomingDocumentDate;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReceiptType getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(ReceiptType receiptType) {
        this.receiptType = receiptType;
    }

    public String getIncomingDocumentNumber() {
        return incomingDocumentNumber;
    }

    public void setIncomingDocumentNumber(String incomingDocumentNumber) {
        this.incomingDocumentNumber = incomingDocumentNumber;
    }

    public java.time.LocalDate getIncomingDocumentDate() {
        return incomingDocumentDate;
    }

    public void setIncomingDocumentDate(java.time.LocalDate incomingDocumentDate) {
        this.incomingDocumentDate = incomingDocumentDate;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.INBOUND;
    }
}
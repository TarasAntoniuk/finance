package com.tarasantoniuk.finance.banking.report.accountturnover.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Schema(
        description = """
                Individual transaction movement representing a single financial event in the account.

                Each movement captures a complete transaction record including:
                - Transaction identification and source document reference
                - Transaction timing and description
                - Financial impact (debit or credit)
                - Running balance after the transaction

                Movements are used for detailed reconciliation and audit trail purposes.
                """,
        example = """
                {
                  "eventId": 80,
                  "documentId": 52,
                  "documentType": "BankReceipt",
                  "documentDate": "2025-01-01T09:00:00",
                  "description": "Payment from customer",
                  "debit": 20000.00,
                  "credit": 0.00,
                  "runningBalance": 20000.00
                }
                """
)
public class MovementDTO {

    @Schema(
            description = """
                    Unique identifier of the transaction event.
                    Used for tracking and referencing specific transactions in the event sourcing system.
                    Can be used to link to detailed transaction records.
                    """,
            example = "80",
            required = true
    )
    private Long eventId;

    @Schema(
            description = """
                    Identifier of the source document that generated this transaction.
                    References the original business document (invoice, payment order, etc.).
                    Can be used to trace back to the originating document.
                    """,
            example = "52",
            required = true
    )
    private Long documentId;

    @Schema(
            description = """
                    Type of the source document that generated this transaction.
                    Common types: BankReceipt, BankPayment, BankTransfer.
                    Indicates the nature of the business operation.
                    """,
            example = "BankReceipt",
            required = true
    )
    private String documentType;

    @Schema(
            description = """
                    Date and time when the transaction occurred.
                    ISO 8601 format: YYYY-MM-DDTHH:mm:ss.
                    Used for sorting movements chronologically.
                    Represents the effective date/time of the financial event.
                    """,
            example = "2025-01-01T09:00:00",
            required = true
    )
    private LocalDateTime documentDate;

    @Schema(
            description = """
                    Human-readable description of the transaction.
                    May contain payment details, counterparty information, or transaction notes.
                    Can be empty if no description was provided.
                    """,
            example = "Payment from customer",
            required = false
    )
    private String description;

    @Schema(
            description = """
                    Debit amount (money received into the account).
                    Positive value indicates money coming in.
                    Increases the account balance.
                    Zero if this is a credit transaction.
                    Decimal precision: 2 decimal places for most currencies.
                    """,
            example = "20000.00",
            required = true,
            minimum = "0"
    )
    private BigDecimal debit;

    @Schema(
            description = """
                    Credit amount (money paid out of the account).
                    Positive value indicates money going out.
                    Decreases the account balance.
                    Zero if this is a debit transaction.
                    Decimal precision: 2 decimal places for most currencies.
                    """,
            example = "0.00",
            required = true,
            minimum = "0"
    )
    private BigDecimal credit;

    @Schema(
            description = """
                    Running balance after this transaction is applied.
                    Calculated as: previous balance + debit - credit.
                    Represents the cumulative balance up to and including this transaction.
                    Used for reconciliation and balance verification.
                    Can be negative if account is overdrawn.
                    """,
            example = "20000.00",
            required = true
    )
    private BigDecimal runningBalance;

    public MovementDTO() {
    }

    public MovementDTO(Long eventId, Long documentId, String documentType, LocalDateTime documentDate,
                       String description, BigDecimal debit, BigDecimal credit, BigDecimal runningBalance) {
        this.eventId = eventId;
        this.documentId = documentId;
        this.documentType = documentType;
        this.documentDate = documentDate;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
        this.runningBalance = runningBalance;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public LocalDateTime getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDateTime documentDate) {
        this.documentDate = documentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovementDTO that = (MovementDTO) o;
        return Objects.equals(eventId, that.eventId) &&
                Objects.equals(documentId, that.documentId) &&
                Objects.equals(documentType, that.documentType) &&
                Objects.equals(documentDate, that.documentDate) &&
                Objects.equals(description, that.description) &&
                Objects.equals(debit, that.debit) &&
                Objects.equals(credit, that.credit) &&
                Objects.equals(runningBalance, that.runningBalance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, documentId, documentType, documentDate, description, debit, credit, runningBalance);
    }

    @Override
    public String toString() {
        return "MovementDTO{" +
                "eventId=" + eventId +
                ", documentId=" + documentId +
                ", documentType='" + documentType + '\'' +
                ", documentDate=" + documentDate +
                ", description='" + description + '\'' +
                ", debit=" + debit +
                ", credit=" + credit +
                ", runningBalance=" + runningBalance +
                '}';
    }
}

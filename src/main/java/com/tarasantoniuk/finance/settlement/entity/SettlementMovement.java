package com.tarasantoniuk.finance.settlement.entity;

import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Append-only settlement movement. Positive amount: we owe the counterparty more; negative: less.
 * Corrections are storno rows with the negated amount; a balance is always a plain sum.
 * The settlement document (invoice or prepayment) carries its dates so an open item can be
 * rebuilt from the movements alone.
 */
@Entity
@Table(
        name = "counterparty_settlement_movements",
        indexes = {
                @Index(name = "idx_settlement_movement_settlement_doc",
                        columnList = "settlement_document_type, settlement_document_id"),
                @Index(name = "idx_settlement_movement_source_doc",
                        columnList = "source_document_type, source_document_id"),
                @Index(name = "idx_settlement_movement_key_datetime",
                        columnList = "organization_id, counterparty_id, currency_id, movement_date_time")
        }
)
public class SettlementMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "counterparty_id", nullable = false)
    private Counterparty counterparty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(name = "settlement_document_type", nullable = false, length = 50)
    private String settlementDocumentType;

    @Column(name = "settlement_document_id", nullable = false)
    private Long settlementDocumentId;

    @Column(name = "settlement_document_date", nullable = false)
    private LocalDate settlementDocumentDate;

    @Column(name = "settlement_due_date")
    private LocalDate settlementDueDate;

    @Column(name = "movement_date_time", nullable = false)
    private LocalDateTime movementDateTime;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "source_document_type", nullable = false, length = 50)
    private String sourceDocumentType;

    @Column(name = "source_document_id", nullable = false)
    private Long sourceDocumentId;

    @Column(name = "storno_of_id")
    private Long stornoOfId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Counterparty getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(Counterparty counterparty) {
        this.counterparty = counterparty;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getSettlementDocumentType() {
        return settlementDocumentType;
    }

    public void setSettlementDocumentType(String settlementDocumentType) {
        this.settlementDocumentType = settlementDocumentType;
    }

    public Long getSettlementDocumentId() {
        return settlementDocumentId;
    }

    public void setSettlementDocumentId(Long settlementDocumentId) {
        this.settlementDocumentId = settlementDocumentId;
    }

    public LocalDate getSettlementDocumentDate() {
        return settlementDocumentDate;
    }

    public void setSettlementDocumentDate(LocalDate settlementDocumentDate) {
        this.settlementDocumentDate = settlementDocumentDate;
    }

    public LocalDate getSettlementDueDate() {
        return settlementDueDate;
    }

    public void setSettlementDueDate(LocalDate settlementDueDate) {
        this.settlementDueDate = settlementDueDate;
    }

    public LocalDateTime getMovementDateTime() {
        return movementDateTime;
    }

    public void setMovementDateTime(LocalDateTime movementDateTime) {
        this.movementDateTime = movementDateTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSourceDocumentType() {
        return sourceDocumentType;
    }

    public void setSourceDocumentType(String sourceDocumentType) {
        this.sourceDocumentType = sourceDocumentType;
    }

    public Long getSourceDocumentId() {
        return sourceDocumentId;
    }

    public void setSourceDocumentId(Long sourceDocumentId) {
        this.sourceDocumentId = sourceDocumentId;
    }

    public Long getStornoOfId() {
        return stornoOfId;
    }

    public void setStornoOfId(Long stornoOfId) {
        this.stornoOfId = stornoOfId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

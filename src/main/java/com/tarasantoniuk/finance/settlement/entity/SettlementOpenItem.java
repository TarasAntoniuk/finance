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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Current open amount of one settlement document: positive for an unpaid invoice, negative for an
 * unused prepayment. Rows exist only while the amount is not zero, so the table holds exactly the
 * open items and stays small however long the history grows.
 */
@Entity
@Table(
        name = "counterparty_settlement_open_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_settlement_open_item_document",
                columnNames = {"settlement_document_type", "settlement_document_id"}),
        indexes = @Index(name = "idx_settlement_open_item_key_due",
                columnList = "organization_id, counterparty_id, currency_id, due_date")
)
public class SettlementOpenItem {

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

    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "open_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal openAmount;

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

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getOpenAmount() {
        return openAmount;
    }

    public void setOpenAmount(BigDecimal openAmount) {
        this.openAmount = openAmount;
    }
}

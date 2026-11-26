package com.tarasantoniuk.finance.purchase.purchaseinvoice.entity;

import com.tarasantoniuk.finance.common.document.entity.BaseDocument;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Invoice received from a supplier for goods or services, with fields named after EN 16931 business
 * terms. Totals are always derived from the lines. Posting increases what the organization owes
 * the supplier in counterparty settlements.
 */
@Entity
@Table(
        name = "purchase_invoices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_purchase_invoice_supplier_number",
                columnNames = {"organization_id", "supplier_id", "supplier_invoice_number"}),
        indexes = {
                @Index(name = "idx_purchase_invoice_org_date", columnList = "organization_id, transaction_date_time"),
                @Index(name = "idx_purchase_invoice_supplier", columnList = "supplier_id")
        }
)
public class PurchaseInvoice extends BaseDocument {

    public static final String DOCUMENT_TYPE = "PurchaseInvoice";

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** BG-4 Seller */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Counterparty supplier;

    /** BT-1 */
    @Column(name = "supplier_invoice_number", nullable = false, length = 50)
    private String supplierInvoiceNumber;

    /** BT-2 */
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /** BT-9 */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** BT-5 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    /** BT-10 */
    @Column(name = "buyer_reference", length = 100)
    private String buyerReference;

    /** BT-109 */
    @Column(name = "total_net", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalNet = BigDecimal.ZERO;

    /** BT-110 */
    @Column(name = "total_vat", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalVat = BigDecimal.ZERO;

    /** BT-112 */
    @Column(name = "total_gross", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalGross = BigDecimal.ZERO;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNumber ASC")
    private List<PurchaseInvoiceLine> lines = new ArrayList<>();

    /**
     * Replaces all lines, numbers them from 1 and recalculates the totals.
     */
    public void replaceLines(List<PurchaseInvoiceLine> newLines) {
        lines.clear();
        int lineNumber = 1;
        for (PurchaseInvoiceLine line : newLines) {
            line.setInvoice(this);
            line.setLineNumber(lineNumber++);
            line.recalculateNet();
            lines.add(line);
        }
        recalculateTotals();
    }

    /**
     * VAT is computed once per category and rate (EN 16931 BG-23), not summed from rounded line
     * amounts, so the totals match an electronic invoice issued by the supplier.
     */
    public List<VatBreakdown> vatBreakdown() {
        Map<VatGroup, BigDecimal> taxableByGroup = new TreeMap<>(VatGroup.ORDER);
        for (PurchaseInvoiceLine line : lines) {
            taxableByGroup.merge(new VatGroup(line.getVatCategory(), line.getVatRate().stripTrailingZeros()),
                    line.getLineNet(), BigDecimal::add);
        }
        return taxableByGroup.entrySet().stream()
                .map(entry -> new VatBreakdown(
                        entry.getKey().category(),
                        entry.getKey().rate().setScale(PurchaseInvoiceLine.AMOUNT_SCALE, RoundingMode.UNNECESSARY),
                        entry.getValue(),
                        entry.getValue().multiply(entry.getKey().rate())
                                .divide(HUNDRED, PurchaseInvoiceLine.AMOUNT_SCALE, RoundingMode.HALF_UP)))
                .toList();
    }

    private void recalculateTotals() {
        List<VatBreakdown> breakdown = vatBreakdown();
        totalNet = breakdown.stream().map(VatBreakdown::taxableAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalVat = breakdown.stream().map(VatBreakdown::vatAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalGross = totalNet.add(totalVat);
    }

    private record VatGroup(VatCategory category, BigDecimal rate) {

        static final Comparator<VatGroup> ORDER = Comparator
                .comparing(VatGroup::category)
                .thenComparing(VatGroup::rate);
    }

    public Long getId() {
        return id;
    }

    public Counterparty getSupplier() {
        return supplier;
    }

    public void setSupplier(Counterparty supplier) {
        this.supplier = supplier;
    }

    public String getSupplierInvoiceNumber() {
        return supplierInvoiceNumber;
    }

    public void setSupplierInvoiceNumber(String supplierInvoiceNumber) {
        this.supplierInvoiceNumber = supplierInvoiceNumber;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getBuyerReference() {
        return buyerReference;
    }

    public void setBuyerReference(String buyerReference) {
        this.buyerReference = buyerReference;
    }

    public BigDecimal getTotalNet() {
        return totalNet;
    }

    public BigDecimal getTotalVat() {
        return totalVat;
    }

    public BigDecimal getTotalGross() {
        return totalGross;
    }

    public List<PurchaseInvoiceLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}

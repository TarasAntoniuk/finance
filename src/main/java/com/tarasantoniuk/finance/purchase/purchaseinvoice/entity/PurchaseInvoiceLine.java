package com.tarasantoniuk.finance.purchase.purchaseinvoice.entity;

import com.tarasantoniuk.finance.core.item.entity.Item;
import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Invoice line (EN 16931 BG-25). The net amount is derived from quantity and price, never entered.
 */
@Entity
@Table(name = "purchase_invoice_lines")
public class PurchaseInvoiceLine {

    static final int AMOUNT_SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private PurchaseInvoice invoice;

    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** BT-154 */
    @Column(length = 500)
    private String description;

    /** BT-129 */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    /** BT-130, UN/ECE Recommendation 20 */
    @Column(name = "unit_code", nullable = false, length = 3)
    private String unitCode;

    /** BT-146 */
    @Column(name = "unit_price_net", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPriceNet;

    /** BT-131 = quantity × unit price, rounded to cents */
    @Column(name = "line_net", nullable = false, precision = 19, scale = 2)
    private BigDecimal lineNet;

    /** BT-151 */
    @Enumerated(EnumType.STRING)
    @Column(name = "vat_category", nullable = false, length = 2)
    private VatCategory vatCategory;

    /** BT-152 */
    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate;

    void recalculateNet() {
        lineNet = quantity.multiply(unitPriceNet).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public PurchaseInvoice getInvoice() {
        return invoice;
    }

    void setInvoice(PurchaseInvoice invoice) {
        this.invoice = invoice;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public BigDecimal getUnitPriceNet() {
        return unitPriceNet;
    }

    public void setUnitPriceNet(BigDecimal unitPriceNet) {
        this.unitPriceNet = unitPriceNet;
    }

    public BigDecimal getLineNet() {
        return lineNet;
    }

    public VatCategory getVatCategory() {
        return vatCategory;
    }

    public void setVatCategory(VatCategory vatCategory) {
        this.vatCategory = vatCategory;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }
}

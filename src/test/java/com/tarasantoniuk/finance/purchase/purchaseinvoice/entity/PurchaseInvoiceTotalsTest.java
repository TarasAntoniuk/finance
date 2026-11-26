package com.tarasantoniuk.finance.purchase.purchaseinvoice.entity;

import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PurchaseInvoiceTotalsTest {

    @Test
    void lineNetIsQuantityTimesPriceRoundedToCents() {
        PurchaseInvoice invoice = invoiceWith(line("3", "33.3333", VatCategory.S, "20.00"));

        assertThat(invoice.getLines().getFirst().getLineNet()).isEqualByComparingTo("100.00");
        assertThat(invoice.getTotalNet()).isEqualByComparingTo("100.00");
        assertThat(invoice.getTotalVat()).isEqualByComparingTo("20.00");
        assertThat(invoice.getTotalGross()).isEqualByComparingTo("120.00");
    }

    @Test
    void vatIsRoundedOncePerGroupNotPerLine() {
        PurchaseInvoice invoice = invoiceWith(
                line("1", "0.02", VatCategory.S, "25.00"),
                line("1", "0.02", VatCategory.S, "25.00"),
                line("1", "0.02", VatCategory.S, "25.00"));

        // per line: 0.005 -> 0.01, three times 0.03; per group: 0.06 x 25% = 0.015 -> 0.02
        assertThat(invoice.getTotalVat()).isEqualByComparingTo("0.02");
        assertThat(invoice.vatBreakdown()).singleElement()
                .satisfies(group -> assertThat(group.taxableAmount()).isEqualByComparingTo("0.06"));
    }

    @Test
    void linesAreGroupedByCategoryAndRate() {
        PurchaseInvoice invoice = invoiceWith(
                line("10", "50.00", VatCategory.S, "20.00"),
                line("2", "100.00", VatCategory.S, "7.00"),
                line("1", "300.00", VatCategory.E, "0.00"),
                line("5", "20.00", VatCategory.S, "20.00"));

        List<VatBreakdown> breakdown = invoice.vatBreakdown();

        assertThat(breakdown).hasSize(3);
        assertThat(breakdown).extracting(VatBreakdown::category, group -> group.rate().toPlainString())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(VatCategory.S, "7.00"),
                        org.assertj.core.groups.Tuple.tuple(VatCategory.S, "20.00"),
                        org.assertj.core.groups.Tuple.tuple(VatCategory.E, "0.00"));
        assertThat(invoice.getTotalNet()).isEqualByComparingTo("1100.00");
        assertThat(invoice.getTotalVat()).isEqualByComparingTo("134.00");
        assertThat(invoice.getTotalGross()).isEqualByComparingTo("1234.00");
    }

    @Test
    void replacingLinesRenumbersAndRecalculates() {
        PurchaseInvoice invoice = invoiceWith(line("1", "10.00", VatCategory.S, "20.00"));

        invoice.replaceLines(List.of(
                line("2", "5.00", VatCategory.Z, "0.00"),
                line("1", "1.00", VatCategory.Z, "0.00")));

        assertThat(invoice.getLines()).extracting(PurchaseInvoiceLine::getLineNumber).containsExactly(1, 2);
        assertThat(invoice.getTotalGross()).isEqualByComparingTo("11.00");
    }

    private static PurchaseInvoice invoiceWith(PurchaseInvoiceLine... lines) {
        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.replaceLines(List.of(lines));
        return invoice;
    }

    private static PurchaseInvoiceLine line(String quantity, String price, VatCategory category, String rate) {
        PurchaseInvoiceLine line = new PurchaseInvoiceLine();
        line.setQuantity(new BigDecimal(quantity));
        line.setUnitPriceNet(new BigDecimal(price));
        line.setVatCategory(category);
        line.setVatRate(new BigDecimal(rate));
        return line;
    }
}

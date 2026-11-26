package com.tarasantoniuk.finance.purchase.purchaseinvoice.entity;

import com.tarasantoniuk.finance.core.item.enums.VatCategory;

import java.math.BigDecimal;

/**
 * One VAT breakdown group (EN 16931 BG-23): lines with the same category and rate.
 *
 * @param taxableAmount BT-116, sum of the group's line net amounts
 * @param vatAmount     BT-117, taxable amount × rate, rounded once per group
 */
public record VatBreakdown(VatCategory category, BigDecimal rate, BigDecimal taxableAmount, BigDecimal vatAmount) {
}

package com.tarasantoniuk.finance.core.item.enums;

import java.math.BigDecimal;

/**
 * VAT category codes used by EN 16931 (UNTDID 5305 subset), business term BT-151.
 * Only the standard rate carries a positive percentage; every other category has a zero rate.
 */
public enum VatCategory {
    /** Standard rate. */
    S,
    /** Zero rated goods. */
    Z,
    /** Exempt from VAT. */
    E,
    /** VAT reverse charge. */
    AE,
    /** Intra-community supply (VAT exempt for EEA). */
    K,
    /** Free export item, VAT not charged. */
    G,
    /** Services outside the scope of tax. */
    O;

    public boolean isConsistentWith(BigDecimal rate) {
        if (rate == null) {
            return false;
        }
        return this == S ? rate.signum() > 0 : rate.signum() == 0;
    }
}

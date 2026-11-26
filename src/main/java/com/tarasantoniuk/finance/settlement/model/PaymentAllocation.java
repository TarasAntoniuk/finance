package com.tarasantoniuk.finance.settlement.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Part of a payment explicitly assigned to an open settlement document (an invoice).
 */
public record PaymentAllocation(DocumentRef settlementDocument, BigDecimal amount) {

    public PaymentAllocation {
        Objects.requireNonNull(settlementDocument, "settlementDocument");
        Objects.requireNonNull(amount, "amount");
    }
}

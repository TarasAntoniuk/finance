package com.tarasantoniuk.finance.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OpenItemResponse(
        String documentType,
        Long documentId,
        String currencyCode,
        LocalDate documentDate,
        LocalDate dueDate,
        BigDecimal openAmount,
        boolean overdue
) {
}

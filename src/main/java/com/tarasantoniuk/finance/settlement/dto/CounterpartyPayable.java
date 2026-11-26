package com.tarasantoniuk.finance.settlement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "What the organization owes one counterparty in one currency")
public record CounterpartyPayable(
        Long organizationId,
        Long counterpartyId,
        String counterpartyName,
        String currencyCode,
        @Schema(description = "Net open amount: positive = we owe, negative = prepaid")
        BigDecimal balance,
        @Schema(description = "Unpaid amount of invoices past their due date")
        BigDecimal overdue,
        @Schema(description = "Number of open invoices and prepayments")
        Long openItems
) {
}

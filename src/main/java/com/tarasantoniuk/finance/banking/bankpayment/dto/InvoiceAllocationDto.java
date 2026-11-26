package com.tarasantoniuk.finance.banking.bankpayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Part of a supplier payment assigned to one purchase invoice")
public record InvoiceAllocationDto(
        @NotNull
        Long purchaseInvoiceId,

        @Schema(example = "400.00")
        @NotNull @Positive @Digits(integer = 17, fraction = 2)
        BigDecimal amount
) {
}

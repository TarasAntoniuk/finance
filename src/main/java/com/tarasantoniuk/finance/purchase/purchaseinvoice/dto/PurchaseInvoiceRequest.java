package com.tarasantoniuk.finance.purchase.purchaseinvoice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Purchase invoice received from a supplier (fields follow EN 16931 business terms)")
public record PurchaseInvoiceRequest(
        @Schema(description = "Buyer organization; required for ADMIN, defaults to the caller's organization otherwise")
        Long organizationId,

        @NotNull
        Long supplierId,

        @Schema(description = "BT-1, the supplier's own invoice number", example = "INV-2026-0042")
        @NotBlank @Size(max = 50)
        String supplierInvoiceNumber,

        @Schema(description = "BT-2", example = "2026-11-20")
        @NotNull
        LocalDate issueDate,

        @Schema(description = "BT-9; payments settle invoices with the earliest due date first", example = "2026-12-20")
        LocalDate dueDate,

        @Schema(description = "Accounting date of the debt; defaults to the start of the issue date")
        LocalDateTime postingDateTime,

        @Schema(description = "BT-5")
        @NotNull
        Long currencyId,

        @Schema(description = "BT-10")
        @Size(max = 100)
        String buyerReference,

        @Schema(description = "BT-22")
        @Size(max = 500)
        String note,

        @NotEmpty @Size(max = 500) @Valid
        List<PurchaseInvoiceLineRequest> lines
) {
}

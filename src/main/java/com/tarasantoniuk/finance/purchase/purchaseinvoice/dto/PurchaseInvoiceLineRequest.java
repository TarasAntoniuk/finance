package com.tarasantoniuk.finance.purchase.purchaseinvoice.dto;

import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Invoice line. Unit code and VAT default to the item's values when omitted.")
public record PurchaseInvoiceLineRequest(
        @NotNull
        Long itemId,

        @Size(max = 500)
        String description,

        @Schema(example = "16")
        @NotNull @Positive @Digits(integer = 15, fraction = 4)
        BigDecimal quantity,

        @Schema(description = "UN/ECE Recommendation 20; defaults to the item's unit", example = "HUR")
        @Pattern(regexp = "[A-Z0-9]{2,3}", message = "must be a UN/ECE Recommendation 20 code, e.g. HUR, DAY, C62")
        String unitCode,

        @Schema(example = "45.00")
        @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 4)
        BigDecimal unitPriceNet,

        @Schema(description = "Defaults to the item's category; give together with vatRate")
        VatCategory vatCategory,

        @Schema(description = "Defaults to the item's rate; give together with vatCategory", example = "20.00")
        @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2)
        BigDecimal vatRate
) {
}

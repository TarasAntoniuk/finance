package com.tarasantoniuk.finance.core.item.dto;

import com.tarasantoniuk.finance.core.item.enums.ItemType;
import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Item catalog entry to create or update")
public record ItemRequest(
        @Schema(example = "CONS-SENIOR-H")
        @NotBlank @Size(max = 50)
        String code,

        @Schema(example = "Senior consultant, hourly")
        @NotBlank @Size(max = 255)
        String name,

        @NotNull
        ItemType type,

        @Schema(description = "UN/ECE Recommendation 20 unit code", example = "HUR")
        @NotBlank @Pattern(regexp = "[A-Z0-9]{2,3}", message = "must be a UN/ECE Recommendation 20 code, e.g. HUR, DAY, C62")
        String unitCode,

        @Schema(description = "EN 16931 VAT category (BT-151)", example = "S")
        @NotNull
        VatCategory vatCategory,

        @Schema(description = "VAT rate in percent (BT-152); positive for S, zero otherwise", example = "20.00")
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2)
        BigDecimal vatRate,

        @Size(max = 1000)
        String description
) {
}

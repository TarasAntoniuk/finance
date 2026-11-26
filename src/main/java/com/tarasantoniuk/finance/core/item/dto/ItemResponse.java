package com.tarasantoniuk.finance.core.item.dto;

import com.tarasantoniuk.finance.core.item.enums.ItemType;
import com.tarasantoniuk.finance.core.item.enums.VatCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String code,
        String name,
        ItemType type,
        String unitCode,
        VatCategory vatCategory,
        BigDecimal vatRate,
        String description,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

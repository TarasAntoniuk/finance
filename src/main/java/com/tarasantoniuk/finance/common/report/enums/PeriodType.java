package com.tarasantoniuk.finance.common.report.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Supported period types for reports.
 * The client sends the period type and date boundaries,
 * and the backend validates and applies them.
 */
@Schema(description = "Report period type")
public enum PeriodType {

    /**
     * Single day period
     */
    @Schema(description = "Single day period")
    DAY,

    /**
     * Month period (first to last day of month)
     */
    @Schema(description = "Month period (first to last day of month)")
    MONTH,

    /**
     * Quarter period (3 months)
     */
    @Schema(description = "Quarter period (3 months)")
    QUARTER,

    /**
     * Year period (January 1 to December 31)
     */
    @Schema(description = "Year period (January 1 to December 31)")
    YEAR,

    /**
     * Custom date range specified by client
     */
    @Schema(description = "Custom date range specified by client")
    CUSTOM
}

package com.tarasantoniuk.finance.banking.report.accountturnover.controller;

import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverReportDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.service.AccountTurnoverReportService;
import com.tarasantoniuk.finance.common.report.dto.ReportPeriodDto;
import com.tarasantoniuk.finance.common.report.enums.PeriodType;
import com.tarasantoniuk.finance.common.report.service.ReportPeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/banking/reports")
@Tag(name = "Banking - Banking Reports", description = "Banking reports and analytics endpoints")
public class AccountTurnoverReportController {

    private final AccountTurnoverReportService reportService;
    private final ReportPeriodService periodService;

    public AccountTurnoverReportController(
            AccountTurnoverReportService reportService,
            ReportPeriodService periodService
    ) {
        this.reportService = reportService;
        this.periodService = periodService;
    }

    @GetMapping("/account-turnovers")
    @Operation(
            summary = "Get account turnover report",
            description = """
                    Generate report showing account turnovers (receipts and payments) for a specified period.

                    Returns:
                    - Opening balance
                    - Debit/credit turnovers
                    - Closing balance
                    - Transaction count for each account

                    Period Types:
                    - DAY: Single day (defaults to today if no date specified)
                    - MONTH: Full month (first to last day)
                    - QUARTER: Full quarter (3 months) - DEFAULT
                    - YEAR: Full year (January 1 to December 31)
                    - CUSTOM: Custom date range (requires both startDate and endDate)

                    The client sends period type and date boundaries, backend validates and applies them.
                    Maximum period: 365 days

                    Examples:
                    - Current quarter: /api/v1/banking/reports/account-turnovers
                    - Specific month: /api/v1/banking/reports/account-turnovers?periodType=MONTH&startDate=2025-10-01
                    - Custom range: /api/v1/banking/reports/account-turnovers?periodType=CUSTOM&startDate=2025-10-01&endDate=2025-12-31
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Report generated successfully",
                            content = @Content(schema = @Schema(implementation = AccountTurnoverReportDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters (invalid period type, missing dates for CUSTOM, or period > 365 days)"
                    )
            }
    )
    public ResponseEntity<AccountTurnoverReportDto> getAccountTurnovers(
            @Parameter(
                    description = "Period type (DAY, MONTH, QUARTER, YEAR, CUSTOM). Default: QUARTER",
                    example = "QUARTER"
            )
            @RequestParam(required = false)
            PeriodType periodType,

            @Parameter(
                    description = "Period start date. For CUSTOM - required. For others - optional (used to determine which day/month/quarter/year). If not provided, defaults to current period.",
                    example = "2025-10-01"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(
                    description = "Period end date. Required only for CUSTOM period type. Ignored for other period types (boundaries calculated automatically).",
                    example = "2025-12-31"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @Parameter(
                    description = "Filter by organization ID (optional)",
                    example = "1"
            )
            @RequestParam(required = false)
            Long organizationId,

            @Parameter(
                    description = "Filter by specific account ID (optional)",
                    example = "1"
            )
            @RequestParam(required = false)
            Long accountId,

            @Parameter(
                    description = "Filter by currency ID (optional)",
                    example = "1"
            )
            @RequestParam(required = false)
            Long currencyId
    ) {
        // Process period parameters - client is the single source of truth
        ReportPeriodDto period = periodService.processPeriod(periodType, startDate, endDate);

        // Generate report with validated period
        AccountTurnoverReportDto report = reportService.generateReport(
                period,
                organizationId,
                accountId,
                currencyId
        );
        return ResponseEntity.ok(report);
    }
}
package com.tarasantoniuk.finance.banking.report.accountturnover.controller;

import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverReportDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.service.AccountTurnoverReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;

@RestController
@RequestMapping("/api/v1/banking/reports")
@Tag(name = "Banking - Banking Reports", description = "Banking reports and analytics endpoints")
public class AccountTurnoverReportController {

    private final AccountTurnoverReportService reportService;

    public AccountTurnoverReportController(AccountTurnoverReportService reportService) {
        this.reportService = reportService;
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
                    
                    Default period: current quarter (Q4 2025: October 1 - December 31)
                    Maximum period: 365 days
                    
                    Example: /api/v1/banking/reports/account-turnovers?startDate=2025-10-01&endDate=2025-12-31&organizationId=1
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Report generated successfully",
                            content = @Content(schema = @Schema(implementation = AccountTurnoverReportDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters (invalid period or period > 365 days)"
                    )
            }
    )
    public ResponseEntity<AccountTurnoverReportDto> getAccountTurnovers(
            @Parameter(
                    description = "Period start date (default: first day of current quarter)",
                    example = "2025-10-01"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(
                    description = "Period end date (default: last day of current quarter)",
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
        // Дефолтні значення: поточний квартал
        LocalDate reportStartDate = startDate != null ? startDate : getQuarterStartDate(LocalDate.now());
        LocalDate reportEndDate = endDate != null ? endDate : getQuarterEndDate(LocalDate.now());

        AccountTurnoverReportDto report = reportService.generateReport(
                reportStartDate, reportEndDate, organizationId, accountId, currencyId
        );
        return ResponseEntity.ok(report);
    }

    /**
     * Отримати першу дату поточного кварталу
     */
    private LocalDate getQuarterStartDate(LocalDate date) {
        int currentMonth = date.getMonthValue();
        int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
        return date.withMonth(quarterStartMonth).withDayOfMonth(1);
    }

    /**
     * Отримати останню дату поточного кварталу
     */
    private LocalDate getQuarterEndDate(LocalDate date) {
        int currentMonth = date.getMonthValue();
        int quarterEndMonth = ((currentMonth - 1) / 3) * 3 + 3;
        return date.withMonth(quarterEndMonth).withDayOfMonth(
                Month.of(quarterEndMonth).length(date.isLeapYear())
        );
    }
}
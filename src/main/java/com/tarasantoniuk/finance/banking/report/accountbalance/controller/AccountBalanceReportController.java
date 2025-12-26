package com.tarasantoniuk.finance.banking.report.accountbalance.controller;

import com.tarasantoniuk.finance.banking.report.accountbalance.dto.AccountBalanceReportDto;
import com.tarasantoniuk.finance.banking.report.accountbalance.service.AccountBalanceReportService;
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
@Tag(name = "Banking - Banking Reports - Account Balance", description = "Banking report Account Balance")
public class AccountBalanceReportController {

    private final AccountBalanceReportService reportService;

    public AccountBalanceReportController(AccountBalanceReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/account-balances")
    @Operation(
            summary = "Get account balance report",
            description = """
                    Generate report showing current balances on all bank accounts with optional filters.
                    
                    Default values:
                    - asOfDate: today's date
                    - All organizations and currencies included if not filtered
                    
                    Example: /api/v1/banking/reports/account-balances?asOfDate=2025-12-02&organizationId=1
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Report generated successfully",
                            content = @Content(schema = @Schema(implementation = AccountBalanceReportDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters"
                    )
            }
    )
    public ResponseEntity<AccountBalanceReportDto> getAccountBalances(
            @Parameter(
                    description = "Date to calculate balances (default: today)",
                    example = "2025-12-02"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate asOfDate,

            @Parameter(
                    description = "Filter by organization ID (optional)",
                    example = "1"
            )
            @RequestParam(required = false)
            Long organizationId,

            @Parameter(
                    description = "Filter by currency ID (optional)",
                    example = "1"
            )
            @RequestParam(required = false)
            Long currencyId
    ) {
        // Якщо дата не передана, використовуємо сьогоднішню
        LocalDate reportDate = asOfDate != null ? asOfDate : LocalDate.now();

        AccountBalanceReportDto report = reportService.generateReport(reportDate, organizationId, currencyId);
        return ResponseEntity.ok(report);
    }
}
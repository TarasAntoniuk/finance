package com.tarasantoniuk.finance.banking.report.accountbalance.controller;

import com.tarasantoniuk.finance.banking.report.accountbalance.dto.AccountBalanceReportDto;
import com.tarasantoniuk.finance.banking.report.accountbalance.service.AccountBalanceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/banking/reports")
@Tag(name = "Banking - Banking Reports", description = "Banking reports and analytics endpoints")
public class AccountBalanceReportController {

    private final AccountBalanceReportService reportService;

    @Autowired
    public AccountBalanceReportController(AccountBalanceReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/account-balances")
    @Operation(
            summary = "Get account balance report",
            description = "Generate report showing current balances on all bank accounts with optional filters. " +
                    "Returns balances as of specified date (default: today).",
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
            @Parameter(description = "Date to calculate balances (default: today)", example = "2024-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate asOfDate,

            @Parameter(description = "Filter by organization ID", example = "1")
            @RequestParam(required = false)
            Long organizationId,

            @Parameter(description = "Filter by currency ID", example = "1")
            @RequestParam(required = false)
            Long currencyId
    ) {
        AccountBalanceReportDto report = reportService.generateReport(asOfDate, organizationId, currencyId);
        return ResponseEntity.ok(report);
    }
}
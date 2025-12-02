package com.tarasantoniuk.finance.banking.report.accountturnover.controller;

import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverReportDto;
import com.tarasantoniuk.finance.banking.report.accountturnover.service.AccountTurnoverReportService;
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
public class AccountTurnoverReportController {

    private final AccountTurnoverReportService reportService;

    @Autowired
    public AccountTurnoverReportController(AccountTurnoverReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/account-turnovers")
    @Operation(
            summary = "Get account turnover report",
            description = "Generate report showing account turnovers (receipts and payments) for a specified period. " +
                    "Returns opening balance, debit/credit turnovers, closing balance, and transaction count for each account. " +
                    "Period cannot exceed 365 days.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Report generated successfully",
                            content = @Content(schema = @Schema(implementation = AccountTurnoverReportDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters (missing dates, invalid period, period > 365 days)"
                    )
            }
    )
    public ResponseEntity<AccountTurnoverReportDto> getAccountTurnovers(
            @Parameter(description = "Period start date (required)", example = "2024-01-01", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(description = "Period end date (required)", example = "2024-01-31", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @Parameter(description = "Filter by organization ID", example = "1")
            @RequestParam(required = false)
            Long organizationId,

            @Parameter(description = "Filter by specific account ID", example = "1")
            @RequestParam(required = false)
            Long accountId,

            @Parameter(description = "Filter by currency ID", example = "1")
            @RequestParam(required = false)
            Long currencyId
    ) {
        AccountTurnoverReportDto report = reportService.generateReport(
                startDate, endDate, organizationId, accountId, currencyId
        );
        return ResponseEntity.ok(report);
    }
}
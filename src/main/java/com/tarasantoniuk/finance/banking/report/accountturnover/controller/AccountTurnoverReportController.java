package com.tarasantoniuk.finance.banking.report.accountturnover.controller;

import com.tarasantoniuk.finance.banking.report.accountturnover.dto.AccountTurnoverDetailsDTO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/banking/reports/accountturnover")
@Tag(name = "Banking - Banking Reports - Account Turnover", description = "Banking report - Account Turnover")
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

    @GetMapping("/account-turnovers/{accountId}/details")
    @Operation(
            summary = "Get detailed account turnover drill-down report",
            description = """
                    Generate a comprehensive drill-down report for a specific bank account showing detailed transaction movements.

                    This endpoint provides a granular view of all account activity within a specified date range, including:
                    - Complete transaction history with running balances
                    - Document references for audit trail
                    - Chronologically sorted movements
                    - Opening and closing balance calculations

                    **Use Cases:**
                    - Detailed account reconciliation
                    - Transaction audit and verification
                    - Financial statement preparation
                    - Account activity analysis

                    **Report Structure:**
                    1. **Account Information**
                       - Account number and name
                       - Bank details (name and SWIFT code)
                       - Currency code

                    2. **Opening Balance**
                       - Calculated from all transactions before the start date
                       - Excludes reversed and reversal transactions

                    3. **Movements (Transactions)**
                       - Event ID and document reference
                       - Transaction date/time
                       - Description (if available)
                       - Debit amount (money in)
                       - Credit amount (money out)
                       - Running balance after each transaction
                       - Sorted by: transaction date ASC, then event ID ASC

                    4. **Closing Balance**
                       - Final running balance after all movements
                       - Equals opening balance if no movements in period

                    **Data Integrity Rules:**
                    - Only includes transactions from organization accounts
                    - Excludes reversed transactions (isReversed = true)
                    - Excludes reversal transactions (documentType ends with "Reversal")
                    - Validates account ownership against organizationId

                    **Balance Calculation:**
                    - Opening Balance = Sum of all transactions before startDate
                    - DEBIT transactions: balance += amount (money in)
                    - CREDIT transactions: balance -= amount (money out)
                    - Running Balance = Opening Balance + cumulative debits - cumulative credits

                    **Example Request:**
                    GET /api/v1/banking/reports/accountturnover/account-turnovers/1/details?startDate=2025-01-01&endDate=2025-12-31&organizationId=1

                    **Example Response:**
                    ```json
                    {
                      "accountId": 1,
                      "accountNumber": "UA123456789012222222222222222222",
                      "accountName": "BBVA (BBVAESMM)",
                      "currency": "EUR",
                      "openingBalance": 0.00,
                      "closingBalance": 29000.00,
                      "movements": [
                        {
                          "eventId": 80,
                          "documentId": 52,
                          "documentType": "BankReceipt",
                          "documentDate": "2025-01-01T09:00:00",
                          "description": "Payment from customer",
                          "debit": 20000.00,
                          "credit": 0.00,
                          "runningBalance": 20000.00
                        },
                        {
                          "eventId": 86,
                          "documentId": 2,
                          "documentType": "BankPayment",
                          "documentDate": "2025-01-02T09:30:00",
                          "description": "Supplier payment",
                          "debit": 0.00,
                          "credit": 500.00,
                          "runningBalance": 19500.00
                        }
                      ]
                    }
                    ```
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Detailed account turnover report generated successfully. Returns complete transaction history with running balances.",
                            content = @Content(schema = @Schema(implementation = AccountTurnoverDetailsDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    Bad Request - Invalid parameters or business rule violation:
                                    - Missing required parameters (accountId, startDate, endDate, organizationId)
                                    - Account does not belong to specified organization
                                    - Account is not an organization account (counterparty accounts not supported)
                                    - Invalid date format (must be ISO 8601: YYYY-MM-DD)
                                    """
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Account not found - The specified accountId does not exist in the system"
                    )
            }
    )
    public ResponseEntity<AccountTurnoverDetailsDTO> getAccountTurnoverDetails(
            @Parameter(
                    description = """
                            Unique identifier of the bank account to analyze.
                            Must be an organization account (not a counterparty account).
                            The account must exist and belong to the specified organizationId.
                            """,
                    example = "1",
                    required = true
            )
            @PathVariable
            Long accountId,

            @Parameter(
                    description = """
                            Start date of the reporting period (inclusive).
                            Format: ISO 8601 date (YYYY-MM-DD).
                            Only transactions on or after this date will be included in movements.
                            Transactions before this date are used for opening balance calculation.
                            """,
                    example = "2025-01-01",
                    required = true
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(
                    description = """
                            End date of the reporting period (inclusive).
                            Format: ISO 8601 date (YYYY-MM-DD).
                            Only transactions on or before this date will be included in movements.
                            Must be on or after the startDate.
                            """,
                    example = "2025-12-31",
                    required = true
            )
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @Parameter(
                    description = """
                            Organization ID that owns the account.
                            Used for security validation to ensure the account belongs to the specified organization.
                            Request will fail if the account doesn't belong to this organization.
                            """,
                    example = "1",
                    required = true
            )
            @RequestParam
            Long organizationId
    ) {
        AccountTurnoverDetailsDTO details = reportService.getAccountTurnoverDetails(
                accountId,
                startDate,
                endDate,
                organizationId
        );
        return ResponseEntity.ok(details);
    }
}
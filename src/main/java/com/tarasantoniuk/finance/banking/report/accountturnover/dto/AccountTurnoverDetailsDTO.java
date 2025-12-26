package com.tarasantoniuk.finance.banking.report.accountturnover.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Schema(
        description = """
                Detailed account turnover drill-down report containing comprehensive transaction history.

                This report provides a complete view of account activity including:
                - Account identification and details
                - Opening balance (before the reporting period)
                - All transaction movements within the period
                - Closing balance (after the reporting period)

                The movements are sorted chronologically and include running balances for reconciliation purposes.
                """,
        example = """
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
                    }
                  ]
                }
                """
)
public class AccountTurnoverDetailsDTO {

    @Schema(
            description = "Unique identifier of the bank account",
            example = "1",
            required = true
    )
    private Long accountId;

    @Schema(
            description = "Bank account number (typically IBAN format)",
            example = "UA123456789012222222222222222222",
            required = true
    )
    private String accountNumber;

    @Schema(
            description = "Account display name formatted as 'BankName (SWIFT)', combining bank name and SWIFT code for easy identification",
            example = "BBVA (BBVAESMM)",
            required = true
    )
    private String accountName;

    @Schema(
            description = "ISO 4217 currency code for the account",
            example = "EUR",
            required = true,
            minLength = 3,
            maxLength = 3
    )
    private String currency;

    @Schema(
            description = """
                    Opening balance at the start of the reporting period.
                    Calculated as the sum of all transactions before the startDate.
                    Excludes reversed and reversal transactions.
                    Represents the account balance before any movements in the selected period.
                    """,
            example = "0.00",
            required = true
    )
    private BigDecimal openingBalance;

    @Schema(
            description = """
                    Closing balance at the end of the reporting period.
                    Calculated as: opening balance + total debits - total credits.
                    Represents the final running balance after all movements.
                    If no movements in period, equals the opening balance.
                    """,
            example = "29000.00",
            required = true
    )
    private BigDecimal closingBalance;

    @Schema(
            description = """
                    Chronologically ordered list of all transaction movements within the reporting period.
                    Each movement includes debit/credit amounts and a running balance.
                    Sorted by transaction date (ascending), then by event ID (ascending).
                    Empty list if no transactions occurred during the period.
                    Excludes reversed and reversal transactions.
                    """,
            required = true
    )
    private List<MovementDTO> movements;

    public AccountTurnoverDetailsDTO() {
    }

    public AccountTurnoverDetailsDTO(Long accountId, String accountNumber, String accountName, String currency,
                                     BigDecimal openingBalance, BigDecimal closingBalance, List<MovementDTO> movements) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.currency = currency;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.movements = movements;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public List<MovementDTO> getMovements() {
        return movements;
    }

    public void setMovements(List<MovementDTO> movements) {
        this.movements = movements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountTurnoverDetailsDTO that = (AccountTurnoverDetailsDTO) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(accountNumber, that.accountNumber) &&
                Objects.equals(accountName, that.accountName) &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(openingBalance, that.openingBalance) &&
                Objects.equals(closingBalance, that.closingBalance) &&
                Objects.equals(movements, that.movements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, accountNumber, accountName, currency, openingBalance, closingBalance, movements);
    }

    @Override
    public String toString() {
        return "AccountTurnoverDetailsDTO{" +
                "accountId=" + accountId +
                ", accountNumber='" + accountNumber + '\'' +
                ", accountName='" + accountName + '\'' +
                ", currency='" + currency + '\'' +
                ", openingBalance=" + openingBalance +
                ", closingBalance=" + closingBalance +
                ", movements=" + movements +
                '}';
    }
}

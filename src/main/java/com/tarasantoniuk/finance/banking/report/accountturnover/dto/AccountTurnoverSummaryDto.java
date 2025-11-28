package com.tarasantoniuk.finance.banking.report.accountturnover.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Objects;

@Schema(description = "Account turnover information for a period")
public class AccountTurnoverSummaryDto {

    @Schema(description = "Bank account ID", example = "1")
    private Long accountId;

    @Schema(description = "Account number", example = "UA123456789012345678901234567")
    private String accountNumber;

    @Schema(description = "Bank name", example = "PrivatBank")
    private String bankName;

    @Schema(description = "Bank SWIFT code", example = "PBANUA2X")
    private String bankSwiftCode;

    @Schema(description = "Currency code", example = "UAH")
    private String currencyCode;

    @Schema(description = "Currency symbol", example = "₴")
    private String currencySymbol;

    @Schema(description = "Organization name", example = "Test Organization")
    private String organizationName;

    @Schema(description = "Organization ID", example = "1")
    private Long organizationId;

    @Schema(description = "Opening balance at period start", example = "0.00")
    private BigDecimal openingBalance;

    @Schema(description = "Total debit turnover (receipts)", example = "150000.00")
    private BigDecimal debitTurnover;

    @Schema(description = "Total credit turnover (payments)", example = "100000.00")
    private BigDecimal creditTurnover;

    @Schema(description = "Closing balance at period end", example = "50000.00")
    private BigDecimal closingBalance;

    @Schema(description = "Number of transactions in period", example = "25")
    private int transactionCount;

    @Schema(description = "Account status", example = "ACTIVE")
    private String accountStatus;

    public AccountTurnoverSummaryDto() {
    }

    public AccountTurnoverSummaryDto(Long accountId, String accountNumber, String bankName,
                                     String bankSwiftCode, String currencyCode, String currencySymbol,
                                     String organizationName, Long organizationId, BigDecimal openingBalance,
                                     BigDecimal debitTurnover, BigDecimal creditTurnover,
                                     BigDecimal closingBalance, int transactionCount, String accountStatus) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.bankSwiftCode = bankSwiftCode;
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.organizationName = organizationName;
        this.organizationId = organizationId;
        this.openingBalance = openingBalance;
        this.debitTurnover = debitTurnover;
        this.creditTurnover = creditTurnover;
        this.closingBalance = closingBalance;
        this.transactionCount = transactionCount;
        this.accountStatus = accountStatus;
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

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankSwiftCode() {
        return bankSwiftCode;
    }

    public void setBankSwiftCode(String bankSwiftCode) {
        this.bankSwiftCode = bankSwiftCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getDebitTurnover() {
        return debitTurnover;
    }

    public void setDebitTurnover(BigDecimal debitTurnover) {
        this.debitTurnover = debitTurnover;
    }

    public BigDecimal getCreditTurnover() {
        return creditTurnover;
    }

    public void setCreditTurnover(BigDecimal creditTurnover) {
        this.creditTurnover = creditTurnover;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountTurnoverSummaryDto that = (AccountTurnoverSummaryDto) o;
        return transactionCount == that.transactionCount &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(accountNumber, that.accountNumber) &&
                Objects.equals(bankName, that.bankName) &&
                Objects.equals(bankSwiftCode, that.bankSwiftCode) &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(currencySymbol, that.currencySymbol) &&
                Objects.equals(organizationName, that.organizationName) &&
                Objects.equals(organizationId, that.organizationId) &&
                Objects.equals(openingBalance, that.openingBalance) &&
                Objects.equals(debitTurnover, that.debitTurnover) &&
                Objects.equals(creditTurnover, that.creditTurnover) &&
                Objects.equals(closingBalance, that.closingBalance) &&
                Objects.equals(accountStatus, that.accountStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, accountNumber, bankName, bankSwiftCode, currencyCode,
                currencySymbol, organizationName, organizationId, openingBalance, debitTurnover,
                creditTurnover, closingBalance, transactionCount, accountStatus);
    }

    @Override
    public String toString() {
        return "AccountTurnoverSummaryDto{" +
                "accountId=" + accountId +
                ", accountNumber='" + accountNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankSwiftCode='" + bankSwiftCode + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", currencySymbol='" + currencySymbol + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", organizationId=" + organizationId +
                ", openingBalance=" + openingBalance +
                ", debitTurnover=" + debitTurnover +
                ", creditTurnover=" + creditTurnover +
                ", closingBalance=" + closingBalance +
                ", transactionCount=" + transactionCount +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }
}
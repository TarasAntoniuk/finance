package com.tarasantoniuk.finance.banking.report.accountbalance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "Account balance information")
public class AccountBalanceItemDto {

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

    @Schema(description = "Current balance", example = "50000.00")
    private BigDecimal balance;

    @Schema(description = "Last transaction date", example = "2024-01-10")
    private LocalDate lastTransactionDate;

    @Schema(description = "Account status", example = "ACTIVE")
    private String accountStatus;

    public AccountBalanceItemDto() {
    }

    public AccountBalanceItemDto(Long accountId, String accountNumber, String bankName,
                                 String bankSwiftCode, String currencyCode, String currencySymbol,
                                 String organizationName, Long organizationId, BigDecimal balance,
                                 LocalDate lastTransactionDate, String accountStatus) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.bankSwiftCode = bankSwiftCode;
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.organizationName = organizationName;
        this.organizationId = organizationId;
        this.balance = balance;
        this.lastTransactionDate = lastTransactionDate;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDate getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(LocalDate lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
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
        AccountBalanceItemDto that = (AccountBalanceItemDto) o;
        return Objects.equals(accountId, that.accountId) &&
                Objects.equals(accountNumber, that.accountNumber) &&
                Objects.equals(bankName, that.bankName) &&
                Objects.equals(bankSwiftCode, that.bankSwiftCode) &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(currencySymbol, that.currencySymbol) &&
                Objects.equals(organizationName, that.organizationName) &&
                Objects.equals(organizationId, that.organizationId) &&
                Objects.equals(balance, that.balance) &&
                Objects.equals(lastTransactionDate, that.lastTransactionDate) &&
                Objects.equals(accountStatus, that.accountStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, accountNumber, bankName, bankSwiftCode, currencyCode,
                currencySymbol, organizationName, organizationId, balance, lastTransactionDate, accountStatus);
    }

    @Override
    public String toString() {
        return "AccountBalanceItemDto{" +
                "accountId=" + accountId +
                ", accountNumber='" + accountNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankSwiftCode='" + bankSwiftCode + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", currencySymbol='" + currencySymbol + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", organizationId=" + organizationId +
                ", balance=" + balance +
                ", lastTransactionDate=" + lastTransactionDate +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }
}
package com.tarasantoniuk.finance.banking.bankaccount.dto;

import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Base DTO class with common fields for BankAccount request and response DTOs
 */
@Schema(description = "Base bank account information")
public abstract class BaseBankAccountDto {

    @Schema(
            description = "Bank account number (IBAN or local format)",
            example = "UA123456789012345678901234567",
            required = true,
            maxLength = 34
    )
    @NotBlank(message = "Account number is required")
    @Size(max = 34, message = "Account number must not exceed 34 characters")
    private String accountNumber;

    @Schema(
            description = "Type of account holder",
            example = "ORGANIZATION",
            required = true,
            allowableValues = {"ORGANIZATION", "COUNTERPARTY"}
    )
    @NotNull(message = "Holder type is required")
    private AccountHolderType holderType;

    @Schema(
            description = "ID of the account holder (organization or counterparty)",
            example = "1",
            required = true
    )
    @NotNull(message = "Holder ID is required")
    private Long holderId;

    @Schema(
            description = "Descriptive name for the account",
            example = "Main Operating Account",
            maxLength = 200
    )
    @Size(max = 200, message = "Account name must not exceed 200 characters")
    private String accountName;

    @Schema(
            description = "Current status of the account",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "CLOSED"}
    )
    private AccountStatus status;

    @Schema(
            description = "Whether this is the default account for the holder",
            example = "true"
    )
    private Boolean isDefault;

    public BaseBankAccountDto() {
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountHolderType getHolderType() {
        return holderType;
    }

    public void setHolderType(AccountHolderType holderType) {
        this.holderType = holderType;
    }

    public Long getHolderId() {
        return holderId;
    }

    public void setHolderId(Long holderId) {
        this.holderId = holderId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
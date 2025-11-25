package com.tarasantoniuk.finance.banking.bankaccount.dto;

import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Base DTO class with common fields for BankAccount request and response DTOs
 */
public abstract class BaseBankAccountDto {

    @NotBlank(message = "Account number is required")
    @Size(max = 34, message = "Account number must not exceed 34 characters")
    private String accountNumber;

    @NotNull(message = "Holder type is required")
    private AccountHolderType holderType;

    @NotNull(message = "Holder ID is required")
    private Long holderId;

    @Size(max = 200, message = "Account name must not exceed 200 characters")
    private String accountName;

    private AccountStatus status;

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
package com.tarasantoniuk.finance.bankaccount.entity;

import com.tarasantoniuk.finance.bank.entity.Bank;
import com.tarasantoniuk.finance.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.common.entity.BaseEntity;
import com.tarasantoniuk.finance.currency.entity.Currency;
import jakarta.persistence.*;

@Entity
@Table(name = "bank_accounts", indexes = {
        @Index(name = "idx_bank_account_number", columnList = "accountNumber"),
        @Index(name = "idx_bank_account_holder", columnList = "holderType,holderId")
})
public class BankAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_account_seq")
    @SequenceGenerator(
            name = "bank_account_seq",
            sequenceName = "bank_account_id_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(nullable = false, unique = true, length = 34)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountHolderType holderType;

    @Column(nullable = false)
    private Long holderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @Column(length = 200)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean isDefault = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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
package com.tarasantoniuk.finance.banking.bankaccount.repository;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findByHolderTypeAndHolderId(AccountHolderType holderType, Long holderId);

    List<BankAccount> findByBankId(Long bankId);

    List<BankAccount> findByStatus(AccountStatus status);

    List<BankAccount> findByHolderTypeAndHolderIdAndIsDefaultTrue(AccountHolderType holderType, Long holderId);
}
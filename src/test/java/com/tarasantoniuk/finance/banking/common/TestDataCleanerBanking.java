package com.tarasantoniuk.finance.banking.common;

import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankaccountbalance.repository.BankAccountBalanceSnapshotRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.repository.BankAccountTransactionEventRepository;
import com.tarasantoniuk.finance.banking.bank.repository.BankRepository;
import com.tarasantoniuk.finance.banking.bankpayment.repository.BankPaymentRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cleaner for banking domain entities in integration tests.
 * Deletes data in correct order to avoid foreign key violations.
 */
@Component
public class TestDataCleanerBanking {

    @Autowired
    private BankAccountTransactionEventRepository transactionEventRepository;

    @Autowired
    private BankAccountBalanceSnapshotRepository balanceSnapshotRepository;

    @Autowired
    private BankPaymentRepository bankPaymentRepository;

    @Autowired
    private BankReceiptRepository bankReceiptRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankRepository bankRepository;

    /**
     * Deletes all banking domain test data in correct order
     * CRITICAL ORDER (most dependent first):
     * 1. TransactionEvents (→ BankAccount, Organization, Currency)
     * 2. BalanceSnapshots (→ BankAccount, Organization, Currency)
     * 3. Payments (→ BankAccount)
     * 4. Receipts (→ BankAccount)
     * 5. BankAccounts (→ Bank, Currency)
     * 6. Banks (→ Country)
     */
    //@Transactional
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanAll() {
        transactionEventRepository.deleteAllInBatch();
        balanceSnapshotRepository.deleteAllInBatch();
        bankPaymentRepository.deleteAllInBatch();
        bankReceiptRepository.deleteAllInBatch();
        bankAccountRepository.deleteAllInBatch();
        bankRepository.deleteAllInBatch();
    }
}
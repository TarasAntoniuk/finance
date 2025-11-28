package com.tarasantoniuk.finance.banking.common;

import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;

import com.tarasantoniuk.finance.banking.bankaccountbalance.repository.BankAccountBalanceSnapshotRepository;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.repository.BankAccountTransactionEventRepository;
import com.tarasantoniuk.finance.banking.bank.repository.BankRepository;
import com.tarasantoniuk.finance.banking.bankpayment.repository.BankPaymentRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
     * Order: TransactionEvents → BalanceSnapshots → Payments → Receipts → BankAccounts → Banks
     */
    public void cleanAll() {
        transactionEventRepository.deleteAll();
        balanceSnapshotRepository.deleteAll();
        bankPaymentRepository.deleteAll();
        bankReceiptRepository.deleteAll();
        bankAccountRepository.deleteAll();
        bankRepository.deleteAll();
    }
}
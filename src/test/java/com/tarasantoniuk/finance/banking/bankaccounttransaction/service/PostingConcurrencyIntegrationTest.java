package com.tarasantoniuk.finance.banking.bankaccounttransaction.service;

import com.tarasantoniuk.finance.banking.bankaccountbalance.service.BankAccountBalanceService;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.repository.BankAccountTransactionEventRepository;
import com.tarasantoniuk.finance.banking.common.exeption.InsufficientBalanceException;
import com.tarasantoniuk.finance.common.document.exception.InvalidDocumentStatusException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankpayment.entity.BankPayment;
import com.tarasantoniuk.finance.banking.bankpayment.repository.BankPaymentRepository;
import com.tarasantoniuk.finance.banking.bankpayment.service.BankPaymentService;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.service.BankReceiptService;
import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for bank account snapshot validity feature.
 * Tests the complete workflow with real services and database.
 */
/**
 * Fires two status changes at the same moment from two threads, against PostgreSQL and without a
 * test-managed transaction, so each call commits on its own like two HTTP requests would.
 */
@SpringBootTest
@DisplayName("Concurrent posting")
class PostingConcurrencyIntegrationTest extends BaseIntegrationTest {

    private static final int REPETITIONS = 5;
    private static final Duration CALL_TIMEOUT = Duration.ofSeconds(30);

    @Autowired private BankReceiptService bankReceiptService;
    @Autowired private BankPaymentService bankPaymentService;
    @Autowired private BankReceiptRepository bankReceiptRepository;
    @Autowired private BankPaymentRepository bankPaymentRepository;
    @Autowired private BankAccountTransactionEventRepository eventRepository;
    @Autowired private BankAccountBalanceService balanceService;
    @Autowired private TestDataFactoryCore factoryCore;
    @Autowired private TestDataFactoryBanking factoryBanking;
    @Autowired private TestDataCleanerCore cleanerCore;
    @Autowired private TestDataCleanerBanking cleanerBanking;

    private Currency currency;
    private Organization organization;
    private Counterparty counterparty;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
        Country country = factoryCore.createUkraine();
        currency = factoryCore.createUAH();
        organization = factoryCore.createDefaultOrganization(country);
        counterparty = factoryCore.createCustomerCounterparty();
        Bank bank = factoryBanking.createPrivatBank(country);
        account = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
    }

    @AfterEach
    void tearDown() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
    }

    @RepeatedTest(REPETITIONS)
    @DisplayName("Posting the same receipt twice at once should write exactly one event")
    void doublePostShouldWriteOneEvent() throws Exception {
        BankReceipt receipt = factoryBanking.createBankReceipt(
                account, counterparty, currency, organization, new BigDecimal("100.00"));

        List<Throwable> failures = runConcurrently(
                () -> bankReceiptService.post(receipt.getId()),
                () -> bankReceiptService.post(receipt.getId()));

        assertThat(eventRepository.findAllByDocumentTypeAndDocumentIdWithRelations("BankReceipt", receipt.getId()))
                .hasSize(1);
        assertThat(failures).hasSize(1).first().isInstanceOf(InvalidDocumentStatusException.class);
        assertThat(balanceService.calculateBalance(account.getId(), LocalDateTime.now()))
                .isEqualByComparingTo("100.00");
    }

    @RepeatedTest(REPETITIONS)
    @DisplayName("Two payments that together exceed the balance should not both be posted")
    void concurrentPaymentsShouldNotOverdraw() throws Exception {
        BankReceipt funding = factoryBanking.createBankReceipt(
                account, counterparty, currency, organization, new BigDecimal("1000.00"));
        funding.setTransactionDateTime(LocalDateTime.now().minusMinutes(5));
        bankReceiptRepository.save(funding);
        bankReceiptService.post(funding.getId());
        BankPayment first = createPayment(new BigDecimal("600.00"));
        BankPayment second = createPayment(new BigDecimal("600.00"));

        List<Throwable> failures = runConcurrently(
                () -> bankPaymentService.post(first.getId()),
                () -> bankPaymentService.post(second.getId()));

        assertThat(failures).hasSize(1).first().isInstanceOf(InsufficientBalanceException.class);
        assertThat(balanceService.calculateBalance(account.getId(), LocalDateTime.now()))
                .isEqualByComparingTo("400.00");
    }

    private BankPayment createPayment(BigDecimal amount) {
        BankPayment payment = factoryBanking.createBankPayment(account, counterparty, currency, organization, amount);
        payment.setTransactionDateTime(LocalDateTime.now().minusMinutes(1));
        return bankPaymentRepository.save(payment);
    }

    /**
     * Starts both calls behind a barrier so they reach the database together, each in its own thread
     * with the caller's authentication. Returns the exceptions the calls threw.
     */
    private List<Throwable> runConcurrently(Runnable firstCall, Runnable secondCall) throws Exception {
        Authentication caller = SecurityContextHolder.getContext().getAuthentication();
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> futures = List.of(
                    executor.submit(asCaller(caller, start, firstCall)),
                    executor.submit(asCaller(caller, start, secondCall)));
            List<Throwable> failures = new ArrayList<>();
            for (Future<?> future : futures) {
                try {
                    future.get(CALL_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    failures.add(e.getCause());
                }
            }
            return failures;
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Void> asCaller(Authentication caller, CyclicBarrier start, Runnable call) {
        return () -> {
            SecurityContextHolder.getContext().setAuthentication(caller);
            try {
                start.await();
                call.run();
                return null;
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }
}

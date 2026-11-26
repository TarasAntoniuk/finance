package com.tarasantoniuk.finance.banking.bankaccountbalance.service;

import com.tarasantoniuk.finance.banking.common.exception.InsufficientBalanceException;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for bank account snapshot validity feature.
 * Tests the complete workflow with real services and database.
 */
/**
 * The balance of a bank account may not be negative on any date, including dates in the past
 * (backdated payments) and in the future (future-dated payments already posted).
 * <p>
 * No test-managed transaction: each service call commits or rolls back on its own, so the
 * tests also prove that a rejected posting leaves nothing behind.
 */
@SpringBootTest
@DisplayName("Balance may not go negative on any date")
class NegativeBalanceRuleIntegrationTest extends BaseIntegrationTest {

    @Autowired private BankAccountBalanceService balanceService;
    @Autowired private BankReceiptService bankReceiptService;
    @Autowired private BankPaymentService bankPaymentService;
    @Autowired private BankReceiptRepository bankReceiptRepository;
    @Autowired private BankPaymentRepository bankPaymentRepository;
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

    @Test
    @DisplayName("A backdated payment larger than the balance on its date is rejected")
    void backdatedPaymentBeyondHistoricalBalanceIsRejected() {
        postReceipt("1000.00", daysAgo(1));
        BankPayment payment = draftPayment("500.00", daysAgo(5));

        assertThatThrownBy(() -> bankPaymentService.post(payment.getId()))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(currentBalance()).isEqualByComparingTo("1000.00");
        assertThat(bankPaymentRepository.findById(payment.getId()).orElseThrow().getStatus())
                .isEqualTo(DocumentStatus.DRAFT);
    }

    @Test
    @DisplayName("A payment equal to the lowest balance from its date on is allowed")
    void paymentEqualToLowestBalanceIsAllowed() {
        postReceipt("1000.00", daysAgo(5));
        postPayment("300.00", daysAgo(2));
        BankPayment payment = draftPayment("700.00", daysAgo(3));

        bankPaymentService.post(payment.getId());

        assertThat(currentBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("A payment today is rejected when a posted future-dated payment needs the money")
    void paymentBlockedByFutureDatedPayment() {
        postReceipt("1000.00", hoursAgo(1));
        postPayment("800.00", LocalDateTime.now().plusDays(2));
        BankPayment payment = draftPayment("300.00", LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> bankPaymentService.post(payment.getId()))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("Unposting a receipt that funds a later payment is rejected")
    void unpostOfFundingReceiptIsRejected() {
        BankReceipt receipt = postReceipt("1000.00", daysAgo(5));
        postPayment("600.00", daysAgo(2));

        assertThatThrownBy(() -> bankReceiptService.unpost(receipt.getId()))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(bankReceiptRepository.findById(receipt.getId()).orElseThrow().getStatus())
                .isEqualTo(DocumentStatus.POSTED);
        assertThat(currentBalance()).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("Unposting a receipt that nothing depends on is allowed")
    void unpostOfUnusedReceiptIsAllowed() {
        postReceipt("1000.00", daysAgo(5));
        BankReceipt extra = postReceipt("200.00", daysAgo(3));
        postPayment("600.00", daysAgo(2));

        bankReceiptService.unpost(extra.getId());

        assertThat(currentBalance()).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("The rule holds when the balance comes from daily snapshots")
    void ruleHoldsWithSnapshots() {
        postReceipt("1000.00", daysAgo(10));
        postPayment("900.00", daysAgo(2));
        for (int i = 9; i >= 1; i--) {
            balanceService.createSnapshot(account.getId(), LocalDate.now().minusDays(i).atStartOfDay());
        }
        BankPayment payment = draftPayment("200.00", daysAgo(6));

        assertThatThrownBy(() -> bankPaymentService.post(payment.getId()))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    private BankReceipt postReceipt(String amount, LocalDateTime dateTime) {
        BankReceipt receipt = factoryBanking.createBankReceipt(
                account, counterparty, currency, organization, new BigDecimal(amount));
        receipt.setTransactionDateTime(dateTime);
        bankReceiptRepository.save(receipt);
        bankReceiptService.post(receipt.getId());
        return receipt;
    }

    private void postPayment(String amount, LocalDateTime dateTime) {
        bankPaymentService.post(draftPayment(amount, dateTime).getId());
    }

    private BankPayment draftPayment(String amount, LocalDateTime dateTime) {
        BankPayment payment = factoryBanking.createBankPayment(
                account, counterparty, currency, organization, new BigDecimal(amount));
        payment.setTransactionDateTime(dateTime);
        return bankPaymentRepository.save(payment);
    }

    private BigDecimal currentBalance() {
        return balanceService.calculateBalance(account.getId(), LocalDateTime.now().plusYears(1));
    }

    private static LocalDateTime daysAgo(int days) {
        return LocalDateTime.now().minusDays(days);
    }

    private static LocalDateTime hoursAgo(int hours) {
        return LocalDateTime.now().minusHours(hours);
    }
}

package com.tarasantoniuk.finance.banking.common;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bank.repository.BankRepository;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.banking.bankpayment.entity.BankPayment;
import com.tarasantoniuk.finance.banking.bankpayment.enums.PaymentType;
import com.tarasantoniuk.finance.banking.bankpayment.repository.BankPaymentRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.enums.ReceiptType;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.service.BankReceiptService;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory for creating banking domain entities in integration tests.
 * All methods create valid, persisted entities ready to use.
 * For invalid test cases, create objects manually in the test itself.
 */
@Component
public class TestDataFactoryBanking {

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankReceiptRepository bankReceiptRepository;

    @Autowired
    private BankPaymentRepository bankPaymentRepository;

    @Autowired
    private BankReceiptService bankReceiptService;

    // Counter for unique account numbers
    private final AtomicLong accountNumberCounter = new AtomicLong(0);

    // ==================== Banks ====================

    /**
     * Creates PrivatBank (Ukraine)
     */
    public Bank createPrivatBank(Country country) {
        Bank bank = new Bank();
        bank.setName("PrivatBank");
        bank.setSwiftCode("PBANUA2X");
        bank.setCountry(country);
        bank.setAddress("1 Hrushevskoho St, Kyiv");
        bank.setPhoneNumber("+380443639999");
        bank.setWebsite("www.privatbank.ua");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    /**
     * Creates Monobank (Ukraine)
     */
    public Bank createMonobank(Country country) {
        Bank bank = new Bank();
        bank.setName("Monobank");
        bank.setSwiftCode("MBNKUA2X");
        bank.setCountry(country);
        bank.setWebsite("www.monobank.ua");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    /**
     * Creates ING Bank (Netherlands/Pan-European)
     */
    public Bank createINGBank(Country country) {
        Bank bank = new Bank();
        bank.setName("ING Bank");
        bank.setSwiftCode("INGBNL2A");
        bank.setCountry(country);
        bank.setWebsite("www.ing.com");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    /**
     * Creates Deutsche Bank (Germany)
     */
    public Bank createDeutscheBank(Country country) {
        Bank bank = new Bank();
        bank.setName("Deutsche Bank");
        bank.setSwiftCode("DEUTDEFF");
        bank.setCountry(country);
        bank.setWebsite("www.db.com");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    /**
     * Creates BNP Paribas (France)
     */
    public Bank createBNPParibas(Country country) {
        Bank bank = new Bank();
        bank.setName("BNP Paribas");
        bank.setSwiftCode("BNPAFRPP");
        bank.setCountry(country);
        bank.setWebsite("www.bnpparibas.com");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    /**
     * Creates Santander (Spain)
     */
    public Bank createSantander(Country country) {
        Bank bank = new Bank();
        bank.setName("Banco Santander");
        bank.setSwiftCode("BSCHESMMXXX");
        bank.setCountry(country);
        bank.setWebsite("www.santander.com");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    /**
     * Creates Revolut (UK/Lithuania - Digital Bank)
     */
    public Bank createRevolut(Country country) {
        Bank bank = new Bank();
        bank.setName("Revolut");
        bank.setSwiftCode("REVOLT21");
        bank.setCountry(country);
        bank.setWebsite("www.revolut.com");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    /**
     * Creates PKO Bank Polski (Poland)
     */
    public Bank createPKOBankPolski(Country country) {
        Bank bank = new Bank();
        bank.setName("PKO Bank Polski");
        bank.setSwiftCode("BPKOPLPW");
        bank.setCountry(country);
        bank.setWebsite("www.pkobp.pl");
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    /**
     * Creates custom bank with specified parameters
     */
    public Bank createBank(String name, String swiftCode, Country country) {
        Bank bank = new Bank();
        bank.setName(name);
        bank.setSwiftCode(swiftCode);
        bank.setCountry(country);
        bank.setIsActive(true);
        return bankRepository.save(bank);
    }

    // ==================== Bank Accounts - ORGANIZATION ====================

    /**
     * Creates bank account for ORGANIZATION with auto-generated account number
     * Used in: payment.setAccount(), receipt.setAccount()
     * Balance is tracked on this account via TransactionEvents!
     */
    public BankAccount createOrganizationBankAccount(
            Bank bank,
            Currency currency,
            Organization organization
    ) {
        String accountNumber = generateAccountNumber("UA", organization.getId());
        return createOrganizationBankAccount(bank, currency, organization, accountNumber);
    }

    /**
     * Creates bank account for ORGANIZATION with custom account number
     * Used in: payment.setAccount(), receipt.setAccount()
     * Balance is tracked on this account via TransactionEvents!
     */
    public BankAccount createOrganizationBankAccount(
            Bank bank,
            Currency currency,
            Organization organization,
            String accountNumber
    ) {
        BankAccount account = new BankAccount();
        account.setBank(bank);
        account.setCurrency(currency);
        account.setHolderId(organization.getId());
        account.setAccountNumber(accountNumber);
        account.setHolderType(AccountHolderType.ORGANIZATION);
        account.setIsDefault(true);
        return bankAccountRepository.save(account);
    }

    // ==================== Bank Accounts - COUNTERPARTY ====================

    /**
     * Creates bank account for COUNTERPARTY with auto-generated account number
     * Used in: payment.setCounterpartyBankAccount(), receipt.setCounterpartyBankAccount()
     * This is just account details - balance is NOT tracked!
     */
    public BankAccount createCounterpartyBankAccount(
            Bank bank,
            Currency currency,
            Counterparty counterparty
    ) {
        String accountNumber = generateAccountNumber("UA", counterparty.getId() + 10000);
        return createCounterpartyBankAccount(bank, currency, counterparty, accountNumber);
    }

    /**
     * Creates bank account for COUNTERPARTY with custom account number
     * Used in: payment.setCounterpartyBankAccount(), receipt.setCounterpartyBankAccount()
     * This is just account details - balance is NOT tracked!
     */
    public BankAccount createCounterpartyBankAccount(
            Bank bank,
            Currency currency,
            Counterparty counterparty,
            String accountNumber
    ) {
        BankAccount account = new BankAccount();
        account.setBank(bank);
        account.setCurrency(currency);
        account.setHolderId(counterparty.getId());
        account.setAccountNumber(accountNumber);
        account.setHolderType(AccountHolderType.COUNTERPARTY);
        account.setIsDefault(true);
        return bankAccountRepository.save(account);
    }

    // ==================== Bank Receipts ====================

    /**
     * Creates valid bank receipt with default amount (10000.00)
     * Status: DRAFT
     * Type: CUSTOMER_PAYMENT
     */
    public BankReceipt createBankReceipt(
            BankAccount organizationAccount,
            Counterparty counterparty,
            Currency currency,
            Organization organization
    ) {
        return createBankReceipt(
                organizationAccount,
                counterparty,
                null,
                currency,
                organization,
                new BigDecimal("10000.00")
        );
    }

    /**
     * Creates bank receipt with custom amount
     * Status: DRAFT
     * Type: CUSTOMER_PAYMENT
     */
    public BankReceipt createBankReceipt(
            BankAccount organizationAccount,
            Counterparty counterparty,
            Currency currency,
            Organization organization,
            BigDecimal amount
    ) {
        return createBankReceipt(
                organizationAccount,
                counterparty,
                null,
                currency,
                organization,
                amount
        );
    }

    /**
     * Creates bank receipt with counterparty account and custom amount
     * Status: DRAFT
     * Type: CUSTOMER_PAYMENT
     */
    public BankReceipt createBankReceipt(
            BankAccount organizationAccount,
            Counterparty counterparty,
            BankAccount counterpartyAccount,
            Currency currency,
            Organization organization,
            BigDecimal amount
    ) {
        BankReceipt receipt = new BankReceipt();
        receipt.setTransactionDateTime(LocalDateTime.now());
        receipt.setReceiptType(ReceiptType.CUSTOMER_PAYMENT);
        receipt.setAmount(amount);
        receipt.setAccount(organizationAccount);
        receipt.setCounterparty(counterparty);
        receipt.setCounterpartyBankAccount(counterpartyAccount);
        receipt.setCurrency(currency);
        receipt.setOrganization(organization);
        receipt.setStatus(DocumentStatus.DRAFT);
        receipt.setDescription("Test receipt");
        return bankReceiptRepository.save(receipt);
    }

    // ==================== Bank Payments ====================

    /**
     * Creates valid bank payment with default amount (10000.00)
     * Status: DRAFT
     * Type: SUPPLIER_PAYMENT
     */
    public BankPayment createBankPayment(
            BankAccount organizationAccount,
            Counterparty counterparty,
            Currency currency,
            Organization organization
    ) {
        return createBankPayment(
                organizationAccount,
                counterparty,
                null,
                currency,
                organization,
                new BigDecimal("10000.00")
        );
    }

    /**
     * Creates bank payment with custom amount
     * Status: DRAFT
     * Type: SUPPLIER_PAYMENT
     */
    public BankPayment createBankPayment(
            BankAccount organizationAccount,
            Counterparty counterparty,
            Currency currency,
            Organization organization,
            BigDecimal amount
    ) {
        return createBankPayment(
                organizationAccount,
                counterparty,
                null,
                currency,
                organization,
                amount
        );
    }

    /**
     * Creates bank payment with counterparty account and custom amount
     * Status: DRAFT
     * Type: SUPPLIER_PAYMENT
     */
    public BankPayment createBankPayment(
            BankAccount organizationAccount,
            Counterparty counterparty,
            BankAccount counterpartyAccount,
            Currency currency,
            Organization organization,
            BigDecimal amount
    ) {
        BankPayment payment = new BankPayment();
        payment.setTransactionDateTime(LocalDateTime.now());
        payment.setPaymentType(PaymentType.SUPPLIER_PAYMENT);
        payment.setAmount(amount);
        payment.setAccount(organizationAccount);
        payment.setCounterparty(counterparty);
        payment.setCounterpartyBankAccount(counterpartyAccount);
        payment.setCurrency(currency);
        payment.setOrganization(organization);
        payment.setStatus(DocumentStatus.DRAFT);
        payment.setDescription("Test payment");
        return bankPaymentRepository.save(payment);
    }

    /**
     * Creates bank payment with external transaction ID
     * Status: DRAFT
     * Type: SUPPLIER_PAYMENT
     */
    public BankPayment createBankPayment(
            BankAccount organizationAccount,
            Counterparty counterparty,
            Currency currency,
            Organization organization,
            String externalTransactionId
    ) {
        BankPayment payment = createBankPayment(
                organizationAccount,
                counterparty,
                null,
                currency,
                organization,
                new BigDecimal("10000.00")
        );

        payment.setExternalTransactionId(externalTransactionId);
        return bankPaymentRepository.save(payment);
    }

    // ==================== Balance Management ====================

    /**
     * Creates initial balance on organization account by posting a bank receipt
     * This is required for payment post operations to succeed (balance validation)
     * <p>
     * Default amount: 50000.00
     *
     * @param organizationAccount Account to create balance on
     * @param counterparty        Counterparty for the receipt
     * @param counterpartyAccount Optional counterparty bank account
     * @param currency            Currency for the receipt
     * @param organization        Organization for the receipt
     * @return Posted bank receipt that created the balance
     */
    public BankReceipt createInitialBalance(
            BankAccount organizationAccount,
            Counterparty counterparty,
            BankAccount counterpartyAccount,
            Currency currency,
            Organization organization
    ) {
        return createInitialBalance(
                organizationAccount,
                counterparty,
                counterpartyAccount,
                currency,
                organization,
                new BigDecimal("50000.00")
        );
    }

    /**
     * Creates initial balance on organization account with custom amount
     * Posts the receipt to actually create the balance via TransactionEvent
     *
     * @param organizationAccount Account to create balance on
     * @param counterparty        Counterparty for the receipt
     * @param counterpartyAccount Optional counterparty bank account
     * @param currency            Currency for the receipt
     * @param organization        Organization for the receipt
     * @param amount              Balance amount to create
     * @return Posted bank receipt that created the balance
     */
    public BankReceipt createInitialBalance(
            BankAccount organizationAccount,
            Counterparty counterparty,
            BankAccount counterpartyAccount,
            Currency currency,
            Organization organization,
            BigDecimal amount
    ) {
        BankReceipt receipt = createBankReceipt(
                organizationAccount,
                counterparty,
                counterpartyAccount,
                currency,
                organization,
                amount
        );

        // Post the receipt to create actual balance via TransactionEvent
        bankReceiptService.post(receipt.getId());

        return receipt;
    }

    // ==================== Helper Methods ====================

    /**
     * Generates Ukrainian IBAN account number with unique counter
     * Format: UA + 27 digits
     * Uses atomic counter to ensure uniqueness across multiple calls
     */
    private String generateAccountNumber(String countryCode, Long id) {
        long counter = accountNumberCounter.incrementAndGet();
        String accountPart = String.format("%027d", id * 1000000 + counter);
        return countryCode + accountPart;
    }
}
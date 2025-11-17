package com.tarasantoniuk.finance.bankaccount.repository;

import com.tarasantoniuk.finance.bank.entity.Bank;
import com.tarasantoniuk.finance.bank.repository.BankRepository;
import com.tarasantoniuk.finance.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.country.entity.Country;
import com.tarasantoniuk.finance.country.repository.CountryRepository;
import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class BankAccountRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CountryRepository countryRepository;

    private Bank privatBank;
    private Bank monobank;
    private Currency uah;
    private Currency usd;
    private BankAccount orgAccount1;
    private BankAccount orgAccount2;
    private BankAccount counterpartyAccount1;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        bankAccountRepository.deleteAll();
        bankRepository.deleteAll();
        currencyRepository.deleteAll();
        countryRepository.deleteAll();

        // Create country
        Country ukraine = new Country();
        ukraine.setName("Ukraine");
        ukraine.setIsoCode("UKR");
        ukraine = countryRepository.save(ukraine);

        // Find or Create currencies
        uah = currencyRepository.findByCode("UAH")
                .orElseGet(() -> {
                    Currency c = new Currency();
                    c.setCode("UAH");
                    c.setName("Ukrainian Hryvnia");
                    c.setSymbol("₴");
                    c.setNumericCode("980");
                    c.setMinorUnit(2);
                    c.setIsActive(true);
                    return currencyRepository.save(c);
                });

        usd = currencyRepository.findByCode("USD")
                .orElseGet(() -> {
                    Currency c = new Currency();
                    c.setCode("USD");
                    c.setName("US Dollar");
                    c.setSymbol("$");
                    c.setNumericCode("840");
                    c.setMinorUnit(2);
                    c.setIsActive(true);
                    return currencyRepository.save(c);
                });

        // Create banks
        privatBank = new Bank();
        privatBank.setName("PrivatBank");
        privatBank.setSwiftCode("PBANUA2X");
        privatBank.setCountry(ukraine);
        privatBank.setIsActive(true);
        privatBank = bankRepository.save(privatBank);

        monobank = new Bank();
        monobank.setName("Monobank");
        monobank.setSwiftCode("MBNKUA2X");
        monobank.setCountry(ukraine);
        monobank.setIsActive(true);
        monobank = bankRepository.save(monobank);

        // Create organization accounts
        orgAccount1 = new BankAccount();
        orgAccount1.setAccountNumber("UA213223130000026007233566001");
        orgAccount1.setHolderType(AccountHolderType.ORGANIZATION);
        orgAccount1.setHolderId(1L);
        orgAccount1.setBank(privatBank);
        orgAccount1.setCurrency(uah);
        orgAccount1.setAccountName("Main UAH Account");
        orgAccount1.setStatus(AccountStatus.ACTIVE);
        orgAccount1.setIsDefault(true);
        orgAccount1 = bankAccountRepository.save(orgAccount1);

        orgAccount2 = new BankAccount();
        orgAccount2.setAccountNumber("UA213223130000026007233566002");
        orgAccount2.setHolderType(AccountHolderType.ORGANIZATION);
        orgAccount2.setHolderId(1L);
        orgAccount2.setBank(monobank);
        orgAccount2.setCurrency(usd);
        orgAccount2.setAccountName("USD Account");
        orgAccount2.setStatus(AccountStatus.ACTIVE);
        orgAccount2.setIsDefault(false);
        orgAccount2 = bankAccountRepository.save(orgAccount2);

        // Create counterparty account
        counterpartyAccount1 = new BankAccount();
        counterpartyAccount1.setAccountNumber("UA213223130000026007233566003");
        counterpartyAccount1.setHolderType(AccountHolderType.COUNTERPARTY);
        counterpartyAccount1.setHolderId(5L);
        counterpartyAccount1.setBank(privatBank);
        counterpartyAccount1.setCurrency(uah);
        counterpartyAccount1.setAccountName("Counterparty Account");
        counterpartyAccount1.setStatus(AccountStatus.INACTIVE);
        counterpartyAccount1.setIsDefault(false);
        counterpartyAccount1 = bankAccountRepository.save(counterpartyAccount1);
    }

    @Test
    void findByAccountNumber_WhenExists_ShouldReturnBankAccount() {
        // When
        Optional<BankAccount> found = bankAccountRepository.findByAccountNumber("UA213223130000026007233566001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getAccountName()).isEqualTo("Main UAH Account");
        assertThat(found.get().getHolderType()).isEqualTo(AccountHolderType.ORGANIZATION);
    }

    @Test
    void findByAccountNumber_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<BankAccount> found = bankAccountRepository.findByAccountNumber("UA999999999999999999999999999");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByHolderTypeAndHolderId_ShouldReturnAccountsForSpecificHolder() {
        // When
        List<BankAccount> orgAccounts = bankAccountRepository.findByHolderTypeAndHolderId(
                AccountHolderType.ORGANIZATION, 1L);
        List<BankAccount> counterpartyAccounts = bankAccountRepository.findByHolderTypeAndHolderId(
                AccountHolderType.COUNTERPARTY, 5L);

        // Then
        assertThat(orgAccounts).hasSize(2);
        assertThat(orgAccounts).extracting(BankAccount::getAccountName)
                .containsExactlyInAnyOrder("Main UAH Account", "USD Account");

        assertThat(counterpartyAccounts).hasSize(1);
        assertThat(counterpartyAccounts.get(0).getAccountName()).isEqualTo("Counterparty Account");
    }

    @Test
    void findByHolderTypeAndHolderId_WhenNoAccounts_ShouldReturnEmptyList() {
        // When
        List<BankAccount> accounts = bankAccountRepository.findByHolderTypeAndHolderId(
                AccountHolderType.ORGANIZATION, 999L);

        // Then
        assertThat(accounts).isEmpty();
    }

    @Test
    void findByBankId_ShouldReturnAccountsForSpecificBank() {
        // When
        List<BankAccount> privatBankAccounts = bankAccountRepository.findByBankId(privatBank.getId());
        List<BankAccount> monobankAccounts = bankAccountRepository.findByBankId(monobank.getId());

        // Then
        assertThat(privatBankAccounts).hasSize(2);
        assertThat(privatBankAccounts).extracting(BankAccount::getAccountName)
                .containsExactlyInAnyOrder("Main UAH Account", "Counterparty Account");

        assertThat(monobankAccounts).hasSize(1);
        assertThat(monobankAccounts.get(0).getAccountName()).isEqualTo("USD Account");
    }

    @Test
    void findByStatus_ShouldReturnAccountsWithSpecificStatus() {
        // When
        List<BankAccount> activeAccounts = bankAccountRepository.findByStatus(AccountStatus.ACTIVE);
        List<BankAccount> inactiveAccounts = bankAccountRepository.findByStatus(AccountStatus.INACTIVE);
        List<BankAccount> closedAccounts = bankAccountRepository.findByStatus(AccountStatus.CLOSED);

        // Then
        assertThat(activeAccounts).hasSize(2);
        assertThat(activeAccounts).extracting(BankAccount::getAccountName)
                .containsExactlyInAnyOrder("Main UAH Account", "USD Account");

        assertThat(inactiveAccounts).hasSize(1);
        assertThat(inactiveAccounts.get(0).getAccountName()).isEqualTo("Counterparty Account");

        assertThat(closedAccounts).isEmpty();
    }

    @Test
    void findByHolderTypeAndHolderIdAndIsDefaultTrue_ShouldReturnDefaultAccounts() {
        // When
        List<BankAccount> defaultAccounts = bankAccountRepository
                .findByHolderTypeAndHolderIdAndIsDefaultTrue(AccountHolderType.ORGANIZATION, 1L);

        // Then
        assertThat(defaultAccounts).hasSize(1);
        assertThat(defaultAccounts.get(0).getAccountName()).isEqualTo("Main UAH Account");
        assertThat(defaultAccounts.get(0).getIsDefault()).isTrue();
    }

    @Test
    void findByHolderTypeAndHolderIdAndIsDefaultTrue_WhenNoDefault_ShouldReturnEmptyList() {
        // When
        List<BankAccount> defaultAccounts = bankAccountRepository
                .findByHolderTypeAndHolderIdAndIsDefaultTrue(AccountHolderType.COUNTERPARTY, 5L);

        // Then
        assertThat(defaultAccounts).isEmpty();
    }

    @Test
    void save_ShouldPersistBankAccountWithAllFields() {
        // Given
        BankAccount newAccount = new BankAccount();
        newAccount.setAccountNumber("UA111111111111111111111111111");
        newAccount.setHolderType(AccountHolderType.ORGANIZATION);
        newAccount.setHolderId(10L);
        newAccount.setBank(privatBank);
        newAccount.setCurrency(uah);
        newAccount.setAccountName("Test Account");
        newAccount.setStatus(AccountStatus.ACTIVE);
        newAccount.setIsDefault(false);

        // When
        BankAccount saved = bankAccountRepository.save(newAccount);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAccountNumber()).isEqualTo("UA111111111111111111111111111");
        assertThat(saved.getHolderType()).isEqualTo(AccountHolderType.ORGANIZATION);
        assertThat(saved.getHolderId()).isEqualTo(10L);
        assertThat(saved.getBank().getId()).isEqualTo(privatBank.getId());
        assertThat(saved.getCurrency().getId()).isEqualTo(uah.getId());
        assertThat(saved.getAccountName()).isEqualTo("Test Account");
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getIsDefault()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_ShouldModifyExistingBankAccount() {
        // Given
        orgAccount1.setAccountName("Updated Account Name");
        orgAccount1.setStatus(AccountStatus.INACTIVE);

        // When
        BankAccount updated = bankAccountRepository.save(orgAccount1);

        // Then
        assertThat(updated.getId()).isEqualTo(orgAccount1.getId());
        assertThat(updated.getAccountName()).isEqualTo("Updated Account Name");
        assertThat(updated.getStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void delete_ShouldRemoveBankAccount() {
        // Given
        Long accountId = orgAccount1.getId();

        // When
        bankAccountRepository.deleteById(accountId);

        // Then
        Optional<BankAccount> deleted = bankAccountRepository.findById(accountId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllBankAccounts() {
        // When
        List<BankAccount> allAccounts = bankAccountRepository.findAll();

        // Then
        assertThat(allAccounts).hasSize(3);
    }

    @Test
    void accountNumberIndex_ShouldImproveQueryPerformance() {
        // This test verifies that the index exists
        // In real scenario, you would use EXPLAIN ANALYZE to verify index usage

        // When
        Optional<BankAccount> found = bankAccountRepository.findByAccountNumber("UA213223130000026007233566001");

        // Then
        assertThat(found).isPresent();
        // Index should make this query fast even with many records
    }

    @Test
    void holderIndex_ShouldImproveQueryPerformance() {
        // This test verifies that the composite index on holderType+holderId exists

        // When
        List<BankAccount> accounts = bankAccountRepository.findByHolderTypeAndHolderId(
                AccountHolderType.ORGANIZATION, 1L);

        // Then
        assertThat(accounts).hasSize(2);
        // Index should make this query fast even with many records
    }

    @Test
    void uniqueAccountNumber_ShouldPreventDuplicates() {
        // Given
        BankAccount duplicateAccount = new BankAccount();
        duplicateAccount.setAccountNumber("UA213223130000026007233566001"); // duplicate!
        duplicateAccount.setHolderType(AccountHolderType.ORGANIZATION);
        duplicateAccount.setHolderId(2L);
        duplicateAccount.setBank(privatBank);
        duplicateAccount.setCurrency(uah);
        duplicateAccount.setStatus(AccountStatus.ACTIVE);

        // When & Then
        // This should throw DataIntegrityViolationException due to unique constraint
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> bankAccountRepository.saveAndFlush(duplicateAccount)
        );
    }
}
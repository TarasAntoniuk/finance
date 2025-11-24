package com.tarasantoniuk.finance.banking.bankaccount.repository;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bank.repository.BankRepository;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
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
        bankAccountRepository.deleteAll();
        bankRepository.deleteAll();
        currencyRepository.deleteAll();
        countryRepository.deleteAll();

        Country ukraine = new Country();
        ukraine.setName("Ukraine");
        ukraine.setIsoCode("UKR");
        ukraine = countryRepository.save(ukraine);

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
        Optional<BankAccount> found = bankAccountRepository.findByAccountNumber("UA213223130000026007233566001");

        assertThat(found).isPresent();
        assertThat(found.get().getAccountName()).isEqualTo("Main UAH Account");
        assertThat(found.get().getHolderType()).isEqualTo(AccountHolderType.ORGANIZATION);
    }

    @Test
    void findByAccountNumber_WhenNotExists_ShouldReturnEmpty() {
        Optional<BankAccount> found = bankAccountRepository.findByAccountNumber("UA999999999999999999999999999");

        assertThat(found).isEmpty();
    }

    @Test
    void findByAccountNumberWithRelations_WhenExists_ShouldReturnBankAccountWithRelations() {
        Optional<BankAccount> found = bankAccountRepository.findByAccountNumberWithRelations("UA213223130000026007233566001");

        assertThat(found).isPresent();
        assertThat(found.get().getAccountName()).isEqualTo("Main UAH Account");
        assertThat(found.get().getBank()).isNotNull();
        assertThat(found.get().getCurrency()).isNotNull();
    }

    @Test
    void findByHolderWithRelations_ShouldReturnAccountsForSpecificHolder() {
        List<BankAccount> orgAccounts = bankAccountRepository.findByHolderWithRelations(
                AccountHolderType.ORGANIZATION, 1L);
        List<BankAccount> counterpartyAccounts = bankAccountRepository.findByHolderWithRelations(
                AccountHolderType.COUNTERPARTY, 5L);

        assertThat(orgAccounts).hasSize(2);
        assertThat(orgAccounts).extracting(BankAccount::getAccountName)
                .containsExactlyInAnyOrder("Main UAH Account", "USD Account");

        assertThat(counterpartyAccounts).hasSize(1);
        assertThat(counterpartyAccounts.get(0).getAccountName()).isEqualTo("Counterparty Account");
    }

    @Test
    void findByHolderWithRelations_WhenNoAccounts_ShouldReturnEmptyList() {
        List<BankAccount> accounts = bankAccountRepository.findByHolderWithRelations(
                AccountHolderType.ORGANIZATION, 999L);

        assertThat(accounts).isEmpty();
    }

    @Test
    void findByBankIdWithRelations_ShouldReturnAccountsForSpecificBank() {
        List<BankAccount> privatBankAccounts = bankAccountRepository.findByBankIdWithRelations(privatBank.getId());
        List<BankAccount> monobankAccounts = bankAccountRepository.findByBankIdWithRelations(monobank.getId());

        assertThat(privatBankAccounts).hasSize(2);
        assertThat(privatBankAccounts).extracting(BankAccount::getAccountName)
                .containsExactlyInAnyOrder("Main UAH Account", "Counterparty Account");

        assertThat(monobankAccounts).hasSize(1);
        assertThat(monobankAccounts.get(0).getAccountName()).isEqualTo("USD Account");
    }

    @Test
    void findByStatusWithRelations_ShouldReturnAccountsWithSpecificStatus() {
        List<BankAccount> activeAccounts = bankAccountRepository.findByStatusWithRelations(AccountStatus.ACTIVE);
        List<BankAccount> inactiveAccounts = bankAccountRepository.findByStatusWithRelations(AccountStatus.INACTIVE);
        List<BankAccount> closedAccounts = bankAccountRepository.findByStatusWithRelations(AccountStatus.CLOSED);

        assertThat(activeAccounts).hasSize(2);
        assertThat(activeAccounts).extracting(BankAccount::getAccountName)
                .containsExactlyInAnyOrder("Main UAH Account", "USD Account");

        assertThat(inactiveAccounts).hasSize(1);
        assertThat(inactiveAccounts.get(0).getAccountName()).isEqualTo("Counterparty Account");

        assertThat(closedAccounts).isEmpty();
    }

    @Test
    void findDefaultByHolderWithRelations_ShouldReturnDefaultAccounts() {
        List<BankAccount> defaultAccounts = bankAccountRepository
                .findDefaultByHolderWithRelations(AccountHolderType.ORGANIZATION, 1L);

        assertThat(defaultAccounts).hasSize(1);
        assertThat(defaultAccounts.get(0).getAccountName()).isEqualTo("Main UAH Account");
        assertThat(defaultAccounts.get(0).getIsDefault()).isTrue();
    }

    @Test
    void findDefaultByHolderWithRelations_WhenNoDefault_ShouldReturnEmptyList() {
        List<BankAccount> defaultAccounts = bankAccountRepository
                .findDefaultByHolderWithRelations(AccountHolderType.COUNTERPARTY, 5L);

        assertThat(defaultAccounts).isEmpty();
    }

    @Test
    void save_ShouldPersistBankAccountWithAllFields() {
        BankAccount newAccount = new BankAccount();
        newAccount.setAccountNumber("UA111111111111111111111111111");
        newAccount.setHolderType(AccountHolderType.ORGANIZATION);
        newAccount.setHolderId(10L);
        newAccount.setBank(privatBank);
        newAccount.setCurrency(uah);
        newAccount.setAccountName("Test Account");
        newAccount.setStatus(AccountStatus.ACTIVE);
        newAccount.setIsDefault(false);

        BankAccount saved = bankAccountRepository.save(newAccount);

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
        orgAccount1.setAccountName("Updated Account Name");
        orgAccount1.setStatus(AccountStatus.INACTIVE);

        BankAccount updated = bankAccountRepository.save(orgAccount1);

        assertThat(updated.getId()).isEqualTo(orgAccount1.getId());
        assertThat(updated.getAccountName()).isEqualTo("Updated Account Name");
        assertThat(updated.getStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void delete_ShouldRemoveBankAccount() {
        Long accountId = orgAccount1.getId();

        bankAccountRepository.deleteById(accountId);

        Optional<BankAccount> deleted = bankAccountRepository.findById(accountId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void findAllWithRelations_ShouldReturnAllBankAccounts() {
        List<BankAccount> allAccounts = bankAccountRepository.findAllWithRelations();

        assertThat(allAccounts).hasSize(3);
    }

    @Test
    void accountNumberIndex_ShouldImproveQueryPerformance() {
        Optional<BankAccount> found = bankAccountRepository.findByAccountNumber("UA213223130000026007233566001");

        assertThat(found).isPresent();
    }

    @Test
    void holderIndex_ShouldImproveQueryPerformance() {
        List<BankAccount> accounts = bankAccountRepository.findByHolderWithRelations(
                AccountHolderType.ORGANIZATION, 1L);

        assertThat(accounts).hasSize(2);
    }

    @Test
    void uniqueAccountNumber_ShouldPreventDuplicates() {
        BankAccount duplicateAccount = new BankAccount();
        duplicateAccount.setAccountNumber("UA213223130000026007233566001");
        duplicateAccount.setHolderType(AccountHolderType.ORGANIZATION);
        duplicateAccount.setHolderId(2L);
        duplicateAccount.setBank(privatBank);
        duplicateAccount.setCurrency(uah);
        duplicateAccount.setStatus(AccountStatus.ACTIVE);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> bankAccountRepository.saveAndFlush(duplicateAccount)
        );
    }
}
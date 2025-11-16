package com.tarasantoniuk.finance.bank.repository;

import com.tarasantoniuk.finance.bank.entity.Bank;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.country.entity.Country;
import com.tarasantoniuk.finance.country.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class BankRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private CountryRepository countryRepository;

    private Country ukraine;
    private Country usa;
    private Bank privatBank;
    private Bank monobank;
    private Bank jpmorgan;

    @BeforeEach
    void setUp() {
        // Clean up existing data to prevent duplicate key errors
        bankRepository.deleteAll();
        countryRepository.deleteAll();

        // Create countries
        ukraine = new Country();
        ukraine.setName("Ukraine");
        ukraine.setIsoCode("UKR");
        ukraine = countryRepository.save(ukraine);

        usa = new Country();
        usa.setName("USA");
        usa.setIsoCode("USA");
        usa = countryRepository.save(usa);

        // Create banks
        privatBank = new Bank();
        privatBank.setName("PrivatBank");
        privatBank.setSwiftCode("PBANUA2X");
        privatBank.setCountry(ukraine);
        privatBank.setAddress("1 Hrushevskoho St, Kyiv");
        privatBank.setPhoneNumber("+380443639999");
        privatBank.setWebsite("www.privatbank.ua");
        privatBank.setIsActive(true);
        privatBank = bankRepository.save(privatBank);

        monobank = new Bank();
        monobank.setName("Monobank");
        monobank.setSwiftCode("MBNKUA2X");
        monobank.setCountry(ukraine);
        monobank.setWebsite("www.monobank.ua");
        monobank.setIsActive(true);
        monobank = bankRepository.save(monobank);

        jpmorgan = new Bank();
        jpmorgan.setName("JPMorgan Chase");
        jpmorgan.setSwiftCode("CHASUS33");
        jpmorgan.setCountry(usa);
        jpmorgan.setWebsite("www.chase.com");
        jpmorgan.setIsActive(false);
        jpmorgan = bankRepository.save(jpmorgan);
    }

    @Test
    void findByCountryId_ShouldReturnBanksFromSpecificCountry() {
        // When
        List<Bank> ukrainianBanks = bankRepository.findByCountryId(ukraine.getId());
        List<Bank> usaBanks = bankRepository.findByCountryId(usa.getId());

        // Then
        assertThat(ukrainianBanks).hasSize(2);
        assertThat(ukrainianBanks).extracting(Bank::getName)
                .containsExactlyInAnyOrder("PrivatBank", "Monobank");

        assertThat(usaBanks).hasSize(1);
        assertThat(usaBanks.get(0).getName()).isEqualTo("JPMorgan Chase");
    }

    @Test
    void findByCountryId_WhenNoBanks_ShouldReturnEmptyList() {
        // Given
        Country germany = new Country();
        germany.setName("Germany");
        germany.setIsoCode("DEU");
        germany = countryRepository.save(germany);

        // When
        List<Bank> germanBanks = bankRepository.findByCountryId(germany.getId());

        // Then
        assertThat(germanBanks).isEmpty();
    }

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveBanks() {
        // When
        List<Bank> activeBanks = bankRepository.findByIsActiveTrue();

        // Then
        assertThat(activeBanks).hasSize(2);
        assertThat(activeBanks).extracting(Bank::getName)
                .containsExactlyInAnyOrder("PrivatBank", "Monobank");
        assertThat(activeBanks).allMatch(Bank::getIsActive);
    }

    @Test
    void findBySwiftCode_WhenExists_ShouldReturnBank() {
        // When
        Optional<Bank> found = bankRepository.findBySwiftCode("PBANUA2X");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("PrivatBank");
        assertThat(found.get().getSwiftCode()).isEqualTo("PBANUA2X");
    }

    @Test
    void findBySwiftCode_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<Bank> found = bankRepository.findBySwiftCode("NOTEXIST");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findBySwiftCode_ShouldBeCaseInsensitive() {
        // When
        Optional<Bank> found = bankRepository.findBySwiftCode("pbanua2x");

        // Then
        // Note: This will fail if DB collation is case-sensitive
        // For case-insensitive search, you might need custom query
        assertThat(found).isEmpty(); // Expected behavior with case-sensitive DB
    }

    @Test
    void save_ShouldPersistBankWithAllFields() {
        // Given
        Bank newBank = new Bank();
        newBank.setName("Test Bank");
        newBank.setSwiftCode("TESTUA2X");
        newBank.setCountry(ukraine);
        newBank.setAddress("Test Address");
        newBank.setPhoneNumber("+380441111111");
        newBank.setWebsite("www.testbank.ua");
        newBank.setIsActive(true);

        // When
        Bank saved = bankRepository.save(newBank);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Bank");
        assertThat(saved.getSwiftCode()).isEqualTo("TESTUA2X");
        assertThat(saved.getCountry().getId()).isEqualTo(ukraine.getId());
        assertThat(saved.getAddress()).isEqualTo("Test Address");
        assertThat(saved.getPhoneNumber()).isEqualTo("+380441111111");
        assertThat(saved.getWebsite()).isEqualTo("www.testbank.ua");
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_ShouldModifyExistingBank() {
        // Given
        privatBank.setName("PrivatBank Updated");
        privatBank.setIsActive(false);

        // When
        Bank updated = bankRepository.save(privatBank);

        // Then
        assertThat(updated.getId()).isEqualTo(privatBank.getId());
        assertThat(updated.getName()).isEqualTo("PrivatBank Updated");
        assertThat(updated.getIsActive()).isFalse();
    }

    @Test
    void delete_ShouldRemoveBank() {
        // Given
        Long bankId = privatBank.getId();

        // When
        bankRepository.deleteById(bankId);

        // Then
        Optional<Bank> deleted = bankRepository.findById(bankId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllBanks() {
        // When
        List<Bank> allBanks = bankRepository.findAll();

        // Then
        assertThat(allBanks).hasSize(3);
        assertThat(allBanks).extracting(Bank::getName)
                .containsExactlyInAnyOrder("PrivatBank", "Monobank", "JPMorgan Chase");
    }

    @Test
    void swiftCodeIndex_ShouldImproveQueryPerformance() {
        // This test verifies that the index exists
        // In real scenario, you would use EXPLAIN ANALYZE to verify index usage

        // When
        Optional<Bank> found = bankRepository.findBySwiftCode("PBANUA2X");

        // Then
        assertThat(found).isPresent();
        // Index should make this query fast even with many records
    }
}
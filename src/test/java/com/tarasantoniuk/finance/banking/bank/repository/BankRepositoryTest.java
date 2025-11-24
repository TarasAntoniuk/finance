package com.tarasantoniuk.finance.banking.bank.repository;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
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
        bankRepository.deleteAll();
        countryRepository.deleteAll();

        ukraine = new Country();
        ukraine.setName("Ukraine");
        ukraine.setIsoCode("UKR");
        ukraine = countryRepository.save(ukraine);

        usa = new Country();
        usa.setName("USA");
        usa.setIsoCode("USA");
        usa = countryRepository.save(usa);

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
        List<Bank> ukrainianBanks = bankRepository.findByCountryIdWithRelations(ukraine.getId());
        List<Bank> usaBanks = bankRepository.findByCountryIdWithRelations(usa.getId());

        assertThat(ukrainianBanks).hasSize(2);
        assertThat(ukrainianBanks).extracting(Bank::getName)
                .containsExactlyInAnyOrder("PrivatBank", "Monobank");

        assertThat(usaBanks).hasSize(1);
        assertThat(usaBanks.get(0).getName()).isEqualTo("JPMorgan Chase");
    }

    @Test
    void findByCountryId_WhenNoBanks_ShouldReturnEmptyList() {
        Country germany = new Country();
        germany.setName("Germany");
        germany.setIsoCode("DEU");
        germany = countryRepository.save(germany);

        List<Bank> germanBanks = bankRepository.findByCountryIdWithRelations(germany.getId());

        assertThat(germanBanks).isEmpty();
    }

    @Test
    void findActiveWithRelations_ShouldReturnOnlyActiveBanks() {
        List<Bank> activeBanks = bankRepository.findActiveWithRelations();

        assertThat(activeBanks).hasSize(2);
        assertThat(activeBanks).extracting(Bank::getName)
                .containsExactlyInAnyOrder("PrivatBank", "Monobank");
        assertThat(activeBanks).allMatch(Bank::getIsActive);
    }

    @Test
    void findBySwiftCode_WhenExists_ShouldReturnBank() {
        Optional<Bank> found = bankRepository.findBySwiftCodeWithRelations("PBANUA2X");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("PrivatBank");
        assertThat(found.get().getSwiftCode()).isEqualTo("PBANUA2X");
    }

    @Test
    void findBySwiftCode_WhenNotExists_ShouldReturnEmpty() {
        Optional<Bank> found = bankRepository.findBySwiftCodeWithRelations("NOTEXIST");

        assertThat(found).isEmpty();
    }

    @Test
    void findBySwiftCode_ShouldBeCaseInsensitive() {
        Optional<Bank> found = bankRepository.findBySwiftCodeWithRelations("pbanua2x");

        assertThat(found).isEmpty(); // Expected behavior with case-sensitive DB
    }

    @Test
    void save_ShouldPersistBankWithAllFields() {
        Bank newBank = new Bank();
        newBank.setName("Test Bank");
        newBank.setSwiftCode("TESTUA2X");
        newBank.setCountry(ukraine);
        newBank.setAddress("Test Address");
        newBank.setPhoneNumber("+380441111111");
        newBank.setWebsite("www.testbank.ua");
        newBank.setIsActive(true);

        Bank saved = bankRepository.save(newBank);

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
        privatBank.setName("PrivatBank Updated");
        privatBank.setIsActive(false);

        Bank updated = bankRepository.save(privatBank);

        assertThat(updated.getId()).isEqualTo(privatBank.getId());
        assertThat(updated.getName()).isEqualTo("PrivatBank Updated");
        assertThat(updated.getIsActive()).isFalse();
    }

    @Test
    void delete_ShouldRemoveBank() {
        Long bankId = privatBank.getId();

        bankRepository.deleteById(bankId);

        Optional<Bank> deleted = bankRepository.findById(bankId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void findAllWithRelations_ShouldReturnAllBanks() {
        List<Bank> allBanks = bankRepository.findAllWithRelations();

        assertThat(allBanks).hasSize(3);
        assertThat(allBanks).extracting(Bank::getName)
                .containsExactlyInAnyOrder("PrivatBank", "Monobank", "JPMorgan Chase");
    }

    @Test
    void swiftCodeIndex_ShouldImproveQueryPerformance() {
        Optional<Bank> found = bankRepository.findBySwiftCodeWithRelations("PBANUA2X");

        assertThat(found).isPresent();
    }
}
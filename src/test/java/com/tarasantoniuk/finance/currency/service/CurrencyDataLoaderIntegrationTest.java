package com.tarasantoniuk.finance.currency.service;

import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DisplayName("Currency Data Loader Integration Tests")
class CurrencyDataLoaderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CurrencyDataLoader currencyDataLoader;

    @BeforeEach
    void setUp() {
        currencyRepository.deleteAll();
    }

    @Test
    @DisplayName("Should load currencies into real database when empty")
    void shouldLoadCurrenciesIntoDatabase() throws Exception {
        // Given
        assertThat(currencyRepository.count()).isZero();

        // When
        currencyDataLoader.run();

        // Then
        assertThat(currencyRepository.count()).isGreaterThan(100);
    }

    @Test
    @DisplayName("Should not load currencies when database already has data")
    void shouldNotLoadWhenDatabaseHasData() throws Exception {
        // Given
        Currency existingCurrency = new Currency();
        existingCurrency.setCode("TST");
        existingCurrency.setNumericCode("999");
        existingCurrency.setName("Test Currency");
        existingCurrency.setSymbol("T");
        existingCurrency.setMinorUnit(2);
        existingCurrency.setIsActive(true);
        currencyRepository.save(existingCurrency);

        long initialCount = currencyRepository.count();

        // When
        currencyDataLoader.run();

        // Then
        assertThat(currencyRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("Should persist all major world currencies")
    void shouldPersistMajorCurrencies() throws Exception {
        // Given
        String[] majorCurrencies = {"USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "UAH"};

        // When
        currencyDataLoader.run();

        // Then
        for (String code : majorCurrencies) {
            Currency currency = currencyRepository.findByCode(code)
                    .orElseThrow(() -> new AssertionError("Currency " + code + " not found"));

            assertThat(currency.getCode()).isEqualTo(code);
            assertThat(currency.getIsActive()).isTrue();
        }
    }

    @Test
    @DisplayName("Should persist UAH with correct properties")
    void shouldPersistUAHCorrectly() throws Exception {
        // When
        currencyDataLoader.run();

        // Then
        Currency uah = currencyRepository.findByCode("UAH")
                .orElseThrow(() -> new AssertionError("UAH not found"));

        assertThat(uah.getCode()).isEqualTo("UAH");
        assertThat(uah.getNumericCode()).isEqualTo("980");
        assertThat(uah.getName()).isEqualTo("Hryvnia");
        assertThat(uah.getSymbol()).isEqualTo("₴");
        assertThat(uah.getMinorUnit()).isEqualTo(2);
        assertThat(uah.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should ensure all currency codes are unique in database")
    void shouldEnsureUniqueCurrencyCodes() throws Exception {
        // When
        currencyDataLoader.run();

        // Then
        List<Currency> currencies = currencyRepository.findAll();
        Set<String> uniqueCodes = currencies.stream()
                .map(Currency::getCode)
                .collect(Collectors.toSet());

        assertThat(uniqueCodes).hasSize(currencies.size());
    }

    @Test
    @DisplayName("Should ensure all numeric codes are unique in database")
    void shouldEnsureUniqueNumericCodes() throws Exception {
        // When
        currencyDataLoader.run();

        // Then
        List<Currency> currencies = currencyRepository.findAll();
        Set<String> uniqueNumericCodes = currencies.stream()
                .map(Currency::getNumericCode)
                .collect(Collectors.toSet());

        assertThat(uniqueNumericCodes).hasSize(currencies.size());
    }

    @Test
    @DisplayName("Should persist currencies with zero minor units")
    void shouldPersistCurrenciesWithZeroMinorUnits() throws Exception {
        // When
        currencyDataLoader.run();

        // Then
        Currency jpy = currencyRepository.findByCode("JPY")
                .orElseThrow(() -> new AssertionError("JPY not found"));
        Currency krw = currencyRepository.findByCode("KRW")
                .orElseThrow(() -> new AssertionError("KRW not found"));
        Currency vnd = currencyRepository.findByCode("VND")
                .orElseThrow(() -> new AssertionError("VND not found"));

        assertThat(jpy.getMinorUnit()).isZero();
        assertThat(krw.getMinorUnit()).isZero();
        assertThat(vnd.getMinorUnit()).isZero();
    }

    @Test
    @DisplayName("Should persist currencies with 3 minor units")
    void shouldPersistCurrenciesWithThreeMinorUnits() throws Exception {
        // When
        currencyDataLoader.run();

        // Then
        Currency kwd = currencyRepository.findByCode("KWD")
                .orElseThrow(() -> new AssertionError("KWD not found"));
        Currency bhd = currencyRepository.findByCode("BHD")
                .orElseThrow(() -> new AssertionError("BHD not found"));
        Currency omr = currencyRepository.findByCode("OMR")
                .orElseThrow(() -> new AssertionError("OMR not found"));
        Currency jod = currencyRepository.findByCode("JOD")
                .orElseThrow(() -> new AssertionError("JOD not found"));

        assertThat(kwd.getMinorUnit()).isEqualTo(3);
        assertThat(bhd.getMinorUnit()).isEqualTo(3);
        assertThat(omr.getMinorUnit()).isEqualTo(3);
        assertThat(jod.getMinorUnit()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should persist cryptocurrencies as inactive")
    void shouldPersistCryptocurrenciesAsInactive() throws Exception {
        // When
        currencyDataLoader.run();

        // Then
        currencyRepository.findByCode("BTC").ifPresent(btc ->
                assertThat(btc.getIsActive()).isFalse()
        );

        currencyRepository.findByCode("ETH").ifPresent(eth ->
                assertThat(eth.getIsActive()).isFalse()
        );
    }

    @Test
    @DisplayName("Should persist precious metals as active")
    void shouldPersistPreciousMetalsAsActive() throws Exception {
        // When
        currencyDataLoader.run();

        // Then
        Currency xau = currencyRepository.findByCode("XAU")
                .orElseThrow(() -> new AssertionError("XAU not found"));
        Currency xag = currencyRepository.findByCode("XAG")
                .orElseThrow(() -> new AssertionError("XAG not found"));
        Currency xpt = currencyRepository.findByCode("XPT")
                .orElseThrow(() -> new AssertionError("XPT not found"));
        Currency xpd = currencyRepository.findByCode("XPD")
                .orElseThrow(() -> new AssertionError("XPD not found"));

        assertThat(xau.getIsActive()).isTrue();
        assertThat(xag.getIsActive()).isTrue();
        assertThat(xpt.getIsActive()).isTrue();
        assertThat(xpd.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should persist European currencies")
    void shouldPersistEuropeanCurrencies() throws Exception {
        // Given
        String[] europeanCurrencies = {"EUR", "UAH", "PLN", "CZK", "HUF", "RON", "BGN", "SEK", "NOK", "DKK"};

        // When
        currencyDataLoader.run();

        // Then
        for (String code : europeanCurrencies) {
            Currency currency = currencyRepository.findByCode(code)
                    .orElseThrow(() -> new AssertionError("Currency " + code + " not found"));
            assertThat(currency.getIsActive()).isTrue();
        }
    }

    @Test
    @DisplayName("Should persist Asian currencies")
    void shouldPersistAsianCurrencies() throws Exception {
        // Given
        String[] asianCurrencies = {"CNY", "JPY", "KRW", "INR", "THB", "SGD", "HKD"};

        // When
        currencyDataLoader.run();

        // Then
        for (String code : asianCurrencies) {
            Currency currency = currencyRepository.findByCode(code)
                    .orElseThrow(() -> new AssertionError("Currency " + code + " not found"));
            assertThat(currency.getIsActive()).isTrue();
        }
    }

    @Test
    @DisplayName("Should persist Latin American currencies")
    void shouldPersistLatinAmericanCurrencies() throws Exception {
        // Given
        String[] latinAmericanCurrencies = {"MXN", "BRL", "ARS", "CLP", "COP", "PEN"};

        // When
        currencyDataLoader.run();

        // Then
        for (String code : latinAmericanCurrencies) {
            Currency currency = currencyRepository.findByCode(code)
                    .orElseThrow(() -> new AssertionError("Currency " + code + " not found"));
            assertThat(currency.getIsActive()).isTrue();
        }
    }

    @Test
    @DisplayName("Should verify all persisted currencies have valid data")
    void shouldVerifyAllCurrenciesHaveValidData() throws Exception {
        // When
        currencyDataLoader.run();

        // Then
        List<Currency> currencies = currencyRepository.findAll();

        assertThat(currencies).isNotEmpty();

        currencies.forEach(currency -> {
            assertThat(currency.getCode())
                    .as("Currency code must be 3 uppercase letters")
                    .matches("^[A-Z]{3}$");

            assertThat(currency.getNumericCode())
                    .as("Numeric code must be 3 digits")
                    .matches("^[0-9]{3}$");

            assertThat(currency.getName())
                    .as("Currency name cannot be blank")
                    .isNotBlank()
                    .hasSizeLessThanOrEqualTo(100);

            assertThat(currency.getMinorUnit())
                    .as("Minor unit must be between 0 and 18")
                    .isBetween(0, 18);

            assertThat(currency.getIsActive())
                    .as("IsActive flag must not be null")
                    .isNotNull();

            assertThat(currency.getId())
                    .as("Currency must have generated ID")
                    .isNotNull();
        });
    }

    @Test
    @DisplayName("Should be able to query currencies by active status")
    void shouldQueryByActiveStatus() throws Exception {
        // When
        currencyDataLoader.run();

        // Then
        List<Currency> activeCurrencies = currencyRepository.findAll().stream()
                .filter(Currency::getIsActive)
                .collect(Collectors.toList());

        List<Currency> inactiveCurrencies = currencyRepository.findAll().stream()
                .filter(c -> !c.getIsActive())
                .collect(Collectors.toList());

        assertThat(activeCurrencies).isNotEmpty();
        assertThat(activeCurrencies.size()).isGreaterThan(inactiveCurrencies.size());
    }

    @Test
    @DisplayName("Should skip loading when database already has data")
    void shouldSkipLoadingWhenDataExists() throws Exception {
        // Given - manually insert one currency before loader runs
        currencyRepository.deleteAll(); // Clean first

        Currency existingCurrency = new Currency();
        existingCurrency.setCode("TST");
        existingCurrency.setNumericCode("999");
        existingCurrency.setName("Test Currency");
        existingCurrency.setSymbol("T");
        existingCurrency.setMinorUnit(2);
        existingCurrency.setIsActive(true);
        currencyRepository.save(existingCurrency);

        long initialCount = currencyRepository.count();
        assertThat(initialCount).isEqualTo(1);

        // When - try to load currencies (should skip because DB has data)
        currencyDataLoader.run();

        // Then - count should remain the same (loader skips if DB not empty)
        assertThat(currencyRepository.count()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("Should verify database indexes work correctly for code lookup")
    void shouldVerifyCodeLookupPerformance() throws Exception {
        // When
        currencyDataLoader.run();

        // Then - should be able to quickly find by code (assuming index exists)
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            currencyRepository.findByCode("USD");
            currencyRepository.findByCode("EUR");
            currencyRepository.findByCode("UAH");
        }

        long duration = System.currentTimeMillis() - startTime;

        // 300 lookups should be fast (less than 1 second with proper indexing)
        assertThat(duration).isLessThan(1000);
    }
}
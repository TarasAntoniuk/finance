package com.tarasantoniuk.finance.externalexchangerate.source.ecb;

import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.externalexchangerate.entity.ExternalExchangeRate;
import com.tarasantoniuk.finance.externalexchangerate.repository.ExternalExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@Transactional
class ECBIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // ВИМКНУТИ ECB sync
        registry.add("ecb.sync.enabled", () -> false);
        registry.add("ecb.sync.initial-load", () -> false);
    }

    @Autowired
    private ECBSyncService syncService;

    @Autowired
    private ExternalExchangeRateRepository rateRepo;

    @Autowired
    private CurrencyRepository currencyRepo;

    @MockitoBean
    private ECBClient client;

    @MockitoBean
    private ECBScheduler scheduler;

    @BeforeEach
    void setUp() {
        rateRepo.deleteAll();
        currencyRepo.deleteAll();

        currencyRepo.save(createCurrency("EUR", "Euro"));
        currencyRepo.save(createCurrency("USD", "US Dollar"));
        currencyRepo.save(createCurrency("GBP", "British Pound"));
    }

    @BeforeEach
    void cleanDatabase() {
        rateRepo.flush();
        currencyRepo.flush();
        rateRepo.deleteAllInBatch();
        currencyRepo.deleteAllInBatch();
    }

    @Test
    void syncDaily_EndToEnd_Success() {
        // Given
        when(client.fetchDaily()).thenReturn(Map.of(
                LocalDate.now(), Map.of("USD", new BigDecimal("1.0850"))
        ));

        // When
        int result = syncService.syncDaily();

        // Then
        assertThat(result).isEqualTo(1);

        List<ExternalExchangeRate> rates = rateRepo.findAll();
        assertThat(rates).hasSize(1);
        assertThat(rates.get(0).getRate()).isEqualByComparingTo("1.0850");
        assertThat(rates.get(0).getSource()).isEqualTo("ECB");
        assertThat(rates.get(0).getCurrencyFrom().getCode()).isEqualTo("EUR");
        assertThat(rates.get(0).getCurrencyTo().getCode()).isEqualTo("USD");
    }

    @Test
    void syncHistory_WithMultipleDates_Success() {
        // Given
        LocalDate date1 = LocalDate.of(2024, 1, 15);
        LocalDate date2 = LocalDate.of(2024, 2, 20);

        when(client.fetchHistory()).thenReturn(Map.of(
                date1, Map.of("USD", new BigDecimal("1.0800")),
                date2, Map.of("USD", new BigDecimal("1.0900"))
        ));

        // When
        int result = syncService.syncHistory();

        // Then
        assertThat(result).isEqualTo(2);

        List<ExternalExchangeRate> rates = rateRepo.findAll();
        assertThat(rates).hasSize(2);
        assertThat(rates).extracting(ExternalExchangeRate::getExchangeDate)
                .containsExactlyInAnyOrder(date1, date2);
    }

    @Test
    void syncDaily_DuplicateRate_Skips() {
        // Given
        LocalDate date = LocalDate.now();
        when(client.fetchDaily()).thenReturn(Map.of(
                date, Map.of("USD", new BigDecimal("1.0850"))
        ));

        // Перший раз - зберегти
        syncService.syncDaily();
        assertThat(rateRepo.count()).isEqualTo(1);

        // When - другий раз з тим самим
        int result = syncService.syncDaily();

        // Then - пропустить дублікат
        assertThat(result).isZero();
        assertThat(rateRepo.count()).isEqualTo(1);
    }

    @Test
    void syncDaily_UnknownCurrency_Skips() {
        // Given
        when(client.fetchDaily()).thenReturn(Map.of(
                LocalDate.now(), Map.of(
                        "USD", new BigDecimal("1.0850"),
                        "XXX", new BigDecimal("999.99")
                )
        ));

        // When
        int result = syncService.syncDaily();

        // Then - тільки USD збережено
        assertThat(result).isEqualTo(1);
        assertThat(rateRepo.count()).isEqualTo(1);
        assertThat(rateRepo.findAll().get(0).getCurrencyTo().getCode()).isEqualTo("USD");
    }

    @Test
    void syncDaily_MultipleCurrencies_SavesAll() {
        // Given
        when(client.fetchDaily()).thenReturn(Map.of(
                LocalDate.now(), Map.of(
                        "USD", new BigDecimal("1.0850"),
                        "GBP", new BigDecimal("0.8345")
                )
        ));

        // When
        int result = syncService.syncDaily();

        // Then
        assertThat(result).isEqualTo(2);
        assertThat(rateRepo.count()).isEqualTo(2);
    }

    @Test
    void syncHistory_LargeDataset_UsesBatchProcessing() {
        // Given - імітуємо 2500 записів (> 2 батча по 1000)
        Map<String, BigDecimal> rates = Map.of(
                "USD", new BigDecimal("1.0850"),
                "GBP", new BigDecimal("0.8345")
        );

        // Створюємо багато дат
        Map<LocalDate, Map<String, BigDecimal>> largeDataset = new java.util.HashMap<>();
        for (int i = 0; i < 1250; i++) {
            largeDataset.put(LocalDate.now().minusDays(i), rates);
        }

        when(client.fetchHistory()).thenReturn(largeDataset);

        // When
        int result = syncService.syncHistory();

        // Then
        assertThat(result).isEqualTo(2500); // 1250 днів * 2 валюти
        assertThat(rateRepo.count()).isEqualTo(2500);
    }

    private Currency createCurrency(String code, String name) {
        Currency c = new Currency();
        c.setCode(code);
        c.setName(name);
        c.setSymbol(code);
        switch (code) {
            case "EUR" -> c.setNumericCode("978");
            case "USD" -> c.setNumericCode("840");
            case "GBP" -> c.setNumericCode("826");
            default -> c.setNumericCode("999");
        }
        c.setMinorUnit(2);
        c.setIsActive(true);
        return c;
    }
}
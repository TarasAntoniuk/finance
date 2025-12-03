package com.tarasantoniuk.finance.core.externalexchangerate.source.ecb;

import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.externalexchangerate.entity.ExternalExchangeRate;
import com.tarasantoniuk.finance.core.externalexchangerate.repository.ExternalExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ECBSyncServiceTest {

    @Mock
    private ECBClient client;

    @Mock
    private ExternalExchangeRateRepository rateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    private ECBSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new ECBSyncService(client, rateRepository, currencyRepository);
    }

    @Test
    void syncDaily_Success() {
        Currency eur = createCurrency(1L, "EUR");
        Currency usd = createCurrency(2L, "USD");

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(currencyRepository.findAll()).thenReturn(List.of(eur, usd));
        when(client.fetchDaily()).thenReturn(Map.of(
                LocalDate.now(), Map.of("USD", new BigDecimal("1.0850"))
        ));
        when(rateRepository.findByExchangeDateBetweenAndSource(any(), any(), eq("ECB")))
                .thenReturn(List.of());

        int result = syncService.syncDaily();

        assertThat(result).isEqualTo(1);
        verify(rateRepository).saveAll(any());
    }

    @Test
    void syncDaily_NullData_ReturnsZero() {
        when(client.fetchDaily()).thenReturn(null);

        int result = syncService.syncDaily();

        assertThat(result).isZero();
        verify(currencyRepository, never()).findByCode(anyString());
        verify(rateRepository, never()).saveAll(any());
    }

    @Test
    void syncDaily_EmptyData_ReturnsZero() {
        when(client.fetchDaily()).thenReturn(Map.of());

        int result = syncService.syncDaily();

        assertThat(result).isZero();
        verify(currencyRepository, never()).findByCode(anyString());
        verify(rateRepository, never()).saveAll(any());
    }

    @Test
    void syncDaily_EURNotFound_ThrowsException() {
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.empty());
        when(client.fetchDaily()).thenReturn(Map.of(
                LocalDate.now(), Map.of("USD", new BigDecimal("1.0850"))
        ));

        assertThatThrownBy(() -> syncService.syncDaily())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("EUR currency not found");
    }

    @Test
    void syncDaily_UnknownCurrency_Skips() {
        Currency eur = createCurrency(1L, "EUR");
        Currency usd = createCurrency(2L, "USD");

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(currencyRepository.findAll()).thenReturn(List.of(eur, usd));
        when(client.fetchDaily()).thenReturn(Map.of(
                LocalDate.now(), Map.of(
                        "USD", new BigDecimal("1.0850"),
                        "XXX", new BigDecimal("999.99")
                )
        ));
        when(rateRepository.findByExchangeDateBetweenAndSource(any(), any(), eq("ECB")))
                .thenReturn(List.of());

        int result = syncService.syncDaily();

        assertThat(result).isEqualTo(1);
        verify(rateRepository).saveAll(argThat(list -> {
            List<?> items = (List<?>) list;
            return items.size() == 1;
        }));
    }

    @Test
    void syncDaily_ExistingRates_Skips() {
        Currency eur = createCurrency(1L, "EUR");
        Currency usd = createCurrency(2L, "USD");
        LocalDate today = LocalDate.now();

        var existingRate = new ExternalExchangeRate();
        existingRate.setExchangeDate(today);
        existingRate.setCurrencyFrom(eur);
        existingRate.setCurrencyTo(usd);

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(currencyRepository.findAll()).thenReturn(List.of(eur, usd));
        when(client.fetchDaily()).thenReturn(Map.of(
                today, Map.of("USD", new BigDecimal("1.0850"))
        ));
        when(rateRepository.findByExchangeDateBetweenAndSource(any(), any(), eq("ECB")))
                .thenReturn(List.of(existingRate));

        int result = syncService.syncDaily();

        assertThat(result).isZero();
        verify(rateRepository, never()).saveAll(any());
    }

    @Test
    void syncDaily_BatchProcessing_SavesInChunks() {
        Currency eur = createCurrency(1L, "EUR");
        List<Currency> currencies = new java.util.ArrayList<>();
        currencies.add(eur);
        for (int i = 2; i <= 35; i++) {
            currencies.add(createCurrency((long) i, "CUR" + i));
        }

        Map<String, BigDecimal> rates = new java.util.HashMap<>();
        for (int i = 2; i <= 35; i++) {
            rates.put("CUR" + i, new BigDecimal("1.0" + i));
        }

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(currencyRepository.findAll()).thenReturn(currencies);
        when(client.fetchDaily()).thenReturn(Map.of(LocalDate.now(), rates));
        when(rateRepository.findByExchangeDateBetweenAndSource(any(), any(), eq("ECB")))
                .thenReturn(List.of());

        int result = syncService.syncDaily();

        assertThat(result).isEqualTo(34);
        verify(rateRepository, atLeastOnce()).saveAll(any());
        verify(rateRepository, atLeastOnce()).flush();
    }

    @Test
    void syncHistory_Success() {
        Currency eur = createCurrency(1L, "EUR");
        Currency usd = createCurrency(2L, "USD");

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(currencyRepository.findAll()).thenReturn(List.of(eur, usd));
        when(client.fetchHistory()).thenReturn(Map.of(
                LocalDate.of(2024, 1, 1), Map.of("USD", new BigDecimal("1.10")),
                LocalDate.of(2024, 1, 2), Map.of("USD", new BigDecimal("1.11"))
        ));
        when(rateRepository.findByExchangeDateBetweenAndSource(any(), any(), eq("ECB")))
                .thenReturn(List.of());

        int result = syncService.syncHistory();

        assertThat(result).isEqualTo(2);
        verify(rateRepository, atLeastOnce()).saveAll(any());
    }

    @Test
    void syncHistory_NullData_ReturnsZero() {
        when(client.fetchHistory()).thenReturn(null);

        int result = syncService.syncHistory();

        assertThat(result).isZero();
        verify(currencyRepository, never()).findByCode(anyString());
        verify(rateRepository, never()).saveAll(any());
    }

    @Test
    void syncHistory_EmptyData_ReturnsZero() {
        when(client.fetchHistory()).thenReturn(Map.of());

        int result = syncService.syncHistory();

        assertThat(result).isZero();
        verify(currencyRepository, never()).findByCode(anyString());
        verify(rateRepository, never()).saveAll(any());
    }

    @Test
    void syncHistory_EURNotFound_ThrowsException() {
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.empty());
        when(client.fetchHistory()).thenReturn(Map.of(
                LocalDate.now(), Map.of("USD", new BigDecimal("1.0850"))
        ));

        assertThatThrownBy(() -> syncService.syncHistory())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("EUR currency not found");
    }

    @Test
    void syncHistory_UnknownCurrency_Skips() {
        Currency eur = createCurrency(1L, "EUR");
        Currency usd = createCurrency(2L, "USD");

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(currencyRepository.findAll()).thenReturn(List.of(eur, usd));
        when(client.fetchHistory()).thenReturn(Map.of(
                LocalDate.of(2024, 1, 1), Map.of(
                        "USD", new BigDecimal("1.10"),
                        "XXX", new BigDecimal("999.99")
                )
        ));
        when(rateRepository.findByExchangeDateBetweenAndSource(any(), any(), eq("ECB")))
                .thenReturn(List.of());

        int result = syncService.syncHistory();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void syncHistory_ExistingRates_Skips() {
        Currency eur = createCurrency(1L, "EUR");
        Currency usd = createCurrency(2L, "USD");
        LocalDate date = LocalDate.of(2024, 1, 1);

        var existingRate = new ExternalExchangeRate();
        existingRate.setExchangeDate(date);
        existingRate.setCurrencyFrom(eur);
        existingRate.setCurrencyTo(usd);

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(currencyRepository.findAll()).thenReturn(List.of(eur, usd));
        when(client.fetchHistory()).thenReturn(Map.of(
                date, Map.of("USD", new BigDecimal("1.10"))
        ));
        when(rateRepository.findByExchangeDateBetweenAndSource(any(), any(), eq("ECB")))
                .thenReturn(List.of(existingRate));

        int result = syncService.syncHistory();

        assertThat(result).isZero();
        verify(rateRepository, never()).saveAll(any());
    }

    @Test
    void syncHistory_LargeBatch_SavesInChunks() {
        Currency eur = createCurrency(1L, "EUR");
        List<Currency> currencies = new java.util.ArrayList<>();
        currencies.add(eur);
        for (int i = 2; i <= 50; i++) {
            currencies.add(createCurrency((long) i, "CUR" + i));
        }

        Map<LocalDate, Map<String, BigDecimal>> historyData = new java.util.HashMap<>();
        for (int day = 1; day <= 25; day++) {
            Map<String, BigDecimal> dailyRates = new java.util.HashMap<>();
            for (int i = 2; i <= 50; i++) {
                dailyRates.put("CUR" + i, new BigDecimal("1.0" + i));
            }
            historyData.put(LocalDate.of(2024, 1, day), dailyRates);
        }

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(currencyRepository.findAll()).thenReturn(currencies);
        when(client.fetchHistory()).thenReturn(historyData);
        when(rateRepository.findByExchangeDateBetweenAndSource(any(), any(), eq("ECB")))
                .thenReturn(List.of());

        int result = syncService.syncHistory();

        assertThat(result).isEqualTo(25 * 49); // 25 днів * 49 валют
        verify(rateRepository, atLeast(2)).saveAll(any()); // Має бути кілька батчів
        verify(rateRepository, atLeast(2)).flush();
    }

    private Currency createCurrency(Long id, String code) {
        Currency c = new Currency();
        c.setId(id);
        c.setCode(code);
        return c;
    }
}
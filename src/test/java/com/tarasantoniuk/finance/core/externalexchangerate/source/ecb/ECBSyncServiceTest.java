package com.tarasantoniuk.finance.core.externalexchangerate.source.ecb;

import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.externalexchangerate.entity.ExternalExchangeRate;
import com.tarasantoniuk.finance.core.externalexchangerate.repository.ExternalExchangeRateRepository;
import com.tarasantoniuk.finance.core.externalexchangerate.source.ecb.ECBClient;
import com.tarasantoniuk.finance.core.externalexchangerate.source.ecb.ECBSyncService;
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

import static org.assertj.core.api.Assertions.*;
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
        // Given
        Currency eur = createCurrency(1L, "EUR");
        Currency usd = createCurrency(2L, "USD");

        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(eur));
        when(currencyRepository.findAll()).thenReturn(List.of(eur, usd));
        when(client.fetchDaily()).thenReturn(Map.of(
                LocalDate.now(), Map.of("USD", new BigDecimal("1.0850"))
        ));
        when(rateRepository.findByExchangeDateBetweenAndSource(any(), any(), eq("ECB")))
                .thenReturn(List.of());

        // When
        int result = syncService.syncDaily();

        // Then
        assertThat(result).isEqualTo(1);
        verify(rateRepository).saveAll(any());
    }

    @Test
    void syncDaily_EURNotFound_ThrowsException() {
        // Given
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.empty());
        when(client.fetchDaily()).thenReturn(Map.of(
                LocalDate.now(), Map.of("USD", new BigDecimal("1.0850"))
        ));

        // When/Then
        assertThatThrownBy(() -> syncService.syncDaily())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("EUR currency not found");
    }

    @Test
    void syncDaily_UnknownCurrency_Skips() {
        // Given
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

        // When
        int result = syncService.syncDaily();

        // Then
        assertThat(result).isEqualTo(1); // Тільки USD
        verify(rateRepository).saveAll(argThat(list -> {
            List<?> items = (List<?>) list;
            return items.size() == 1;
        }));
    }

    @Test
    void syncHistory_MultipleRates() {
        // Given
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

        // When
        int result = syncService.syncHistory();

        // Then
        assertThat(result).isEqualTo(2);
        verify(rateRepository, atLeastOnce()).saveAll(any());
    }

    @Test
    void syncDaily_ExistingRates_Skips() {
        // Given
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

        // When
        int result = syncService.syncDaily();

        // Then
        assertThat(result).isZero(); // Все пропущено
        verify(rateRepository, never()).saveAll(any());
    }

    @Test
    void syncDaily_EmptyData_ReturnsZero() {
        // Given
        when(client.fetchDaily()).thenReturn(Map.of());

        // When
        int result = syncService.syncDaily();

        // Then
        assertThat(result).isZero();
        verify(rateRepository, never()).saveAll(any());
    }

    @Test
    void syncDaily_BatchProcessing_SavesInChunks() {
        // Given - багато курсів
        Currency eur = createCurrency(1L, "EUR");
        List<Currency> currencies = List.of(eur);
        for (int i = 2; i <= 35; i++) {
            currencies = new java.util.ArrayList<>(currencies);
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

        // When
        int result = syncService.syncDaily();

        // Then
        assertThat(result).isEqualTo(34);
        verify(rateRepository, atLeastOnce()).saveAll(any());
        verify(rateRepository, atLeastOnce()).flush();
    }

    private Currency createCurrency(Long id, String code) {
        Currency c = new Currency();
        c.setId(id);
        c.setCode(code);
        return c;
    }
}
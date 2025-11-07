package com.tarasantoniuk.finance.currency.service;

import com.tarasantoniuk.finance.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Currency Data Loader Tests")
class CurrencyDataLoaderTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyDataLoader currencyDataLoader;

    @Captor
    private ArgumentCaptor<CurrencyRequestDTO> currencyCaptor;

    @BeforeEach
    void setUp() {
        reset(currencyRepository, currencyService);
    }

    @Test
    @DisplayName("Should load currencies when database is empty")
    void shouldLoadCurrenciesWhenDatabaseEmpty() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyRepository, times(1)).count();
        verify(currencyService, atLeast(1)).createCurrency(any(CurrencyRequestDTO.class));
    }

    @Test
    @DisplayName("Should skip loading when currencies already exist")
    void shouldSkipLoadingWhenCurrenciesExist() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(50L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyRepository, times(1)).count();
        verify(currencyService, never()).createCurrency(any(CurrencyRequestDTO.class));
    }

    @Test
    @DisplayName("Should load all required major world currencies")
    void shouldLoadMajorCurrenciesCorrectly() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);
        String[] requiredMajorCurrencies = {"USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD"};

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<String> loadedCodes = currencyCaptor.getAllValues().stream()
                .map(CurrencyRequestDTO::getCode)
                .collect(Collectors.toList());

        assertThat(loadedCodes).containsAll(List.of(requiredMajorCurrencies));
    }

    @Test
    @DisplayName("Should load UAH with correct properties")
    void shouldLoadUAHCorrectly() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        CurrencyRequestDTO uah = currencyCaptor.getAllValues().stream()
                .filter(c -> "UAH".equals(c.getCode()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("UAH not found in loaded currencies"));

        assertThat(uah.getNumericCode()).isEqualTo("980");
        assertThat(uah.getName()).isEqualTo("Hryvnia");
        assertThat(uah.getSymbol()).isEqualTo("₴");
        assertThat(uah.getMinorUnit()).isEqualTo(2);
        assertThat(uah.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should validate all currency codes have correct format")
    void shouldValidateCurrencyCodesFormat() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        currencyCaptor.getAllValues().forEach(currency -> {
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
                    .isNotNull()
                    .isBetween(0, 18);

            assertThat(currency.getIsActive())
                    .as("IsActive flag must not be null")
                    .isNotNull();
        });
    }

    @Test
    @DisplayName("Should ensure all currency codes are unique")
    void shouldEnsureUniqueCurrencyCodes() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<CurrencyRequestDTO> currencies = currencyCaptor.getAllValues();
        Set<String> uniqueCodes = currencies.stream()
                .map(CurrencyRequestDTO::getCode)
                .collect(Collectors.toSet());

        assertThat(uniqueCodes)
                .as("All currency codes must be unique")
                .hasSize(currencies.size());
    }

    @Test
    @DisplayName("Should ensure all numeric codes are unique")
    void shouldEnsureUniqueNumericCodes() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<CurrencyRequestDTO> currencies = currencyCaptor.getAllValues();
        Set<String> uniqueNumericCodes = currencies.stream()
                .map(CurrencyRequestDTO::getNumericCode)
                .collect(Collectors.toSet());

        assertThat(uniqueNumericCodes)
                .as("All numeric codes must be unique")
                .hasSize(currencies.size());
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully and continue loading")
    void shouldHandleServiceExceptionsGracefully() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);
        when(currencyService.createCurrency(any(CurrencyRequestDTO.class)))
                .thenThrow(new RuntimeException("Database error"))
                .thenReturn(null)
                .thenReturn(null);

        // When
        currencyDataLoader.run();

        // Then - should not throw exception, just log errors
        verify(currencyRepository, times(1)).count();
        verify(currencyService, atLeast(3)).createCurrency(any(CurrencyRequestDTO.class));
    }

    @Test
    @DisplayName("Should load currencies with zero minor units")
    void shouldLoadCurrenciesWithZeroMinorUnits() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<String> zeroMinorUnitCurrencies = currencyCaptor.getAllValues().stream()
                .filter(c -> c.getMinorUnit() == 0)
                .map(CurrencyRequestDTO::getCode)
                .collect(Collectors.toList());

        assertThat(zeroMinorUnitCurrencies)
                .as("Should load currencies without decimal places")
                .contains("JPY", "KRW", "VND");
    }

    @Test
    @DisplayName("Should load currencies with 2 minor units")
    void shouldLoadCurrenciesWithTwoMinorUnits() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        long twoMinorUnitsCount = currencyCaptor.getAllValues().stream()
                .filter(c -> c.getMinorUnit() == 2)
                .count();

        assertThat(twoMinorUnitsCount)
                .as("Most currencies should have 2 decimal places")
                .isGreaterThan(10);
    }

    @Test
    @DisplayName("Should load currencies with 3 minor units for Middle Eastern currencies")
    void shouldLoadCurrenciesWithThreeMinorUnits() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<String> threeMinorUnitCurrencies = currencyCaptor.getAllValues().stream()
                .filter(c -> c.getMinorUnit() == 3)
                .map(CurrencyRequestDTO::getCode)
                .collect(Collectors.toList());

        assertThat(threeMinorUnitCurrencies)
                .as("Should load Middle Eastern currencies with 3 decimal places")
                .contains("KWD", "BHD", "OMR", "JOD");
    }

    @Test
    @DisplayName("Should load European region currencies")
    void shouldLoadEuropeanCurrencies() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);
        String[] europeanCurrencies = {"EUR", "UAH", "PLN", "CZK", "HUF", "RON", "BGN", "SEK", "NOK", "DKK"};

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<String> loadedCodes = currencyCaptor.getAllValues().stream()
                .map(CurrencyRequestDTO::getCode)
                .collect(Collectors.toList());

        assertThat(loadedCodes)
                .as("Should load all major European currencies")
                .containsAll(List.of(europeanCurrencies));
    }

    @Test
    @DisplayName("Should mark cryptocurrencies as inactive")
    void shouldMarkCryptoCurrenciesAsInactive() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<CurrencyRequestDTO> cryptoCurrencies = currencyCaptor.getAllValues().stream()
                .filter(c -> c.getCode().matches("(BTC|ETH)"))
                .collect(Collectors.toList());

        cryptoCurrencies.forEach(crypto -> {
            assertThat(crypto.getIsActive())
                    .as("Cryptocurrency %s should be marked as inactive", crypto.getCode())
                    .isFalse();
        });
    }

    @Test
    @DisplayName("Should load precious metals codes")
    void shouldLoadPreciousMetalsCodes() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);
        String[] preciousMetals = {"XAU", "XAG", "XPT", "XPD"};

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<String> loadedCodes = currencyCaptor.getAllValues().stream()
                .map(CurrencyRequestDTO::getCode)
                .collect(Collectors.toList());

        assertThat(loadedCodes)
                .as("Should load all precious metals codes")
                .containsAll(List.of(preciousMetals));
    }

    @Test
    @DisplayName("Should mark precious metals as active")
    void shouldMarkPreciousMetalsAsActive() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<CurrencyRequestDTO> metals = currencyCaptor.getAllValues().stream()
                .filter(c -> c.getCode().matches("X(AU|AG|PT|PD)"))
                .collect(Collectors.toList());

        metals.forEach(metal -> {
            assertThat(metal.getIsActive())
                    .as("Precious metal %s should be active", metal.getCode())
                    .isTrue();
        });
    }

    @Test
    @DisplayName("Should load Asian currencies")
    void shouldLoadAsianCurrencies() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);
        String[] asianCurrencies = {"CNY", "JPY", "KRW", "INR", "THB", "SGD", "HKD"};

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<String> loadedCodes = currencyCaptor.getAllValues().stream()
                .map(CurrencyRequestDTO::getCode)
                .collect(Collectors.toList());

        assertThat(loadedCodes)
                .as("Should load major Asian currencies")
                .containsAll(List.of(asianCurrencies));
    }

    @Test
    @DisplayName("Should load Latin American currencies")
    void shouldLoadLatinAmericanCurrencies() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);
        String[] latinAmericanCurrencies = {"MXN", "BRL", "ARS", "CLP", "COP", "PEN"};

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<String> loadedCodes = currencyCaptor.getAllValues().stream()
                .map(CurrencyRequestDTO::getCode)
                .collect(Collectors.toList());

        assertThat(loadedCodes)
                .as("Should load major Latin American currencies")
                .containsAll(List.of(latinAmericanCurrencies));
    }

    @Test
    @DisplayName("Should have symbols for most currencies except special codes")
    void shouldHaveSymbolsForMostCurrencies() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        long currenciesWithSymbols = currencyCaptor.getAllValues().stream()
                .filter(c -> c.getSymbol() != null && !c.getSymbol().isEmpty())
                .count();

        long totalCurrencies = currencyCaptor.getAllValues().size();
        long expectedMinimum = (long) (totalCurrencies * 0.8);

        assertThat(currenciesWithSymbols)
                .as("Most currencies should have symbols defined")
                .isGreaterThan(expectedMinimum); // At least 80% should have symbols
    }

    @Test
    @DisplayName("Should check that no currency is null")
    void shouldCheckNoNullCurrencies() throws Exception {
        // Given
        when(currencyRepository.count()).thenReturn(0L);

        // When
        currencyDataLoader.run();

        // Then
        verify(currencyService, atLeastOnce()).createCurrency(currencyCaptor.capture());

        List<CurrencyRequestDTO> currencies = currencyCaptor.getAllValues();

        assertThat(currencies)
                .as("No currency should be null")
                .doesNotContainNull();
    }
}
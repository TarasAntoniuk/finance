package com.tarasantoniuk.finance.externalexchangerate.service;

import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateRequestDTO;
import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateResponseDTO;
import com.tarasantoniuk.finance.externalexchangerate.entity.ExternalExchangeRate;
import com.tarasantoniuk.finance.externalexchangerate.exception.ExchangeRateAlreadyExistsException;
import com.tarasantoniuk.finance.externalexchangerate.exception.ExchangeRateNotFoundException;
import com.tarasantoniuk.finance.externalexchangerate.exception.InvalidExchangeRateException;
import com.tarasantoniuk.finance.externalexchangerate.mapper.ExternalExchangeRateMapper;
import com.tarasantoniuk.finance.externalexchangerate.repository.ExternalExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalExchangeRateServiceTest {

    @Mock
    private ExternalExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExternalExchangeRateMapper exchangeRateMapper;

    @InjectMocks
    private ExternalExchangeRateService exchangeRateService;

    private ExternalExchangeRate exchangeRate;
    private ExternalExchangeRateRequestDTO requestDTO;
    private ExternalExchangeRateResponseDTO responseDTO;
    private Currency usd;
    private Currency eur;

    @BeforeEach
    void setUp() {
        usd = new Currency();
        usd.setId(1L);
        usd.setCode("USD");

        eur = new Currency();
        eur.setId(2L);
        eur.setCode("EUR");

        exchangeRate = new ExternalExchangeRate();
        exchangeRate.setId(1L);
        exchangeRate.setExchangeDate(LocalDate.now());
        exchangeRate.setCurrencyFrom(usd);
        exchangeRate.setCurrencyTo(eur);
        exchangeRate.setRate(BigDecimal.valueOf(0.92));
        exchangeRate.setSource("ECB");
        exchangeRate.setIsActive(true);

        requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(BigDecimal.valueOf(0.92));
        requestDTO.setSource("ECB");

        responseDTO = new ExternalExchangeRateResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setExchangeDate(LocalDate.now());
        responseDTO.setRate(BigDecimal.valueOf(0.92));
    }

    @Test
    void getExchangeRateById_WhenExists_ShouldReturnRate() {
        // Given
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        // When
        ExternalExchangeRateResponseDTO result = exchangeRateService.getExchangeRateById(1L);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(0.92), result.getRate());
    }

    @Test
    void getExchangeRateById_WhenNotExists_ShouldThrowException() {
        // Given
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.getExchangeRateById(1L));
    }

    @Test
    void createExchangeRate_WhenValid_ShouldReturnCreatedRate() {
        // Given
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(true);
        when(exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                any(), anyLong(), anyLong(), anyString())).thenReturn(false);
        when(exchangeRateMapper.toEntity(requestDTO)).thenReturn(exchangeRate);
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        // When
        ExternalExchangeRateResponseDTO result = exchangeRateService.createExchangeRate(requestDTO);

        // Then
        assertNotNull(result);
        verify(exchangeRateRepository, times(1)).save(any(ExternalExchangeRate.class));
    }

    @Test
    void createExchangeRate_WhenSameCurrency_ShouldThrowException() {
        // Given
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(1L);
        when(currencyRepository.existsById(1L)).thenReturn(true);

        // When & Then
        assertThrows(InvalidExchangeRateException.class,
                () -> exchangeRateService.createExchangeRate(requestDTO));
        verify(exchangeRateRepository, never()).save(any(ExternalExchangeRate.class));
    }

    @Test
    void createExchangeRate_WhenAlreadyExists_ShouldThrowException() {
        // Given
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(true);
        when(exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                any(), anyLong(), anyLong(), anyString())).thenReturn(true);

        // When & Then
        assertThrows(ExchangeRateAlreadyExistsException.class,
                () -> exchangeRateService.createExchangeRate(requestDTO));
    }

    @Test
    void calculateCrossRate_WhenRatesExist_ShouldReturnCalculatedRate() {
        // Given
        LocalDate date = LocalDate.now();
        ExternalExchangeRate rate1 = new ExternalExchangeRate();
        rate1.setRate(BigDecimal.valueOf(1.2)); // USD to GBP

        ExternalExchangeRate rate2 = new ExternalExchangeRate();
        rate2.setRate(BigDecimal.valueOf(0.9)); // GBP to EUR

        when(exchangeRateRepository.findLatestRateBeforeDate(date, 1L, 3L))
                .thenReturn(Arrays.asList(rate1));
        when(exchangeRateRepository.findLatestRateBeforeDate(date, 3L, 2L))
                .thenReturn(Arrays.asList(rate2));

        // When
        BigDecimal result = exchangeRateService.calculateCrossRate(1L, 2L, 3L, date);

        // Then
        assertNotNull(result);
        // 1.2 * 0.9 = 1.08
        assertEquals(0, result.compareTo(BigDecimal.valueOf(1.08)));
    }

    @Test
    void calculateCrossRate_WhenFirstRateMissing_ShouldThrowException() {
        // Given
        LocalDate date = LocalDate.now();
        when(exchangeRateRepository.findLatestRateBeforeDate(date, 1L, 3L))
                .thenReturn(Arrays.asList());

        // When & Then
        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.calculateCrossRate(1L, 2L, 3L, date));
    }

    @Test
    void activateExchangeRate_WhenExists_ShouldActivateSuccessfully() {
        // Given
        exchangeRate.setIsActive(false);
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        // When
        ExternalExchangeRateResponseDTO result = exchangeRateService.activateExchangeRate(1L);

        // Then
        assertTrue(exchangeRate.getIsActive());
        verify(exchangeRateRepository, times(1)).save(exchangeRate);
    }
}
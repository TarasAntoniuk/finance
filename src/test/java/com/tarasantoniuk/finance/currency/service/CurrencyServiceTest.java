package com.tarasantoniuk.finance.currency.service;

import com.tarasantoniuk.finance.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.currency.dto.CurrencyResponseDTO;
import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.exception.CurrencyAlreadyExistsException;
import com.tarasantoniuk.finance.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CurrencyMapper currencyMapper;

    @InjectMocks
    private CurrencyService currencyService;

    private Currency currency;
    private CurrencyRequestDTO requestDTO;
    private CurrencyResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        currency = new Currency();
        currency.setId(1L);
        currency.setCode("USD");
        currency.setNumericCode("840");
        currency.setName("US Dollar");
        currency.setSymbol("$");
        currency.setMinorUnit(2);
        currency.setIsActive(true);

        requestDTO = new CurrencyRequestDTO();
        requestDTO.setCode("USD");
        requestDTO.setNumericCode("840");
        requestDTO.setName("US Dollar");
        requestDTO.setSymbol("$");
        requestDTO.setMinorUnit(2);

        responseDTO = new CurrencyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setCode("USD");
        responseDTO.setNumericCode("840");
    }

    @Test
    void getCurrencyById_WhenExists_ShouldReturnCurrency() {
        // Given
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        // When
        CurrencyResponseDTO result = currencyService.getCurrencyById(1L);

        // Then
        assertNotNull(result);
        assertEquals("USD", result.getCode());
    }

    @Test
    void getCurrencyById_WhenNotExists_ShouldThrowException() {
        // Given
        when(currencyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CurrencyNotFoundException.class,
                () -> currencyService.getCurrencyById(1L));
    }

    @Test
    void getCurrencyByCode_WhenExists_ShouldReturnCurrency() {
        // Given
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(currency));
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        // When
        CurrencyResponseDTO result = currencyService.getCurrencyByCode("USD");

        // Then
        assertNotNull(result);
        assertEquals("USD", result.getCode());
    }

    @Test
    void createCurrency_WhenValid_ShouldReturnCreatedCurrency() {
        // Given
        when(currencyRepository.existsByCode("USD")).thenReturn(false);
        when(currencyRepository.existsByNumericCode("840")).thenReturn(false);
        when(currencyMapper.toEntity(requestDTO)).thenReturn(currency);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        // When
        CurrencyResponseDTO result = currencyService.createCurrency(requestDTO);

        // Then
        assertNotNull(result);
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    void createCurrency_WhenCodeExists_ShouldThrowException() {
        // Given
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        // When & Then
        assertThrows(CurrencyAlreadyExistsException.class,
                () -> currencyService.createCurrency(requestDTO));
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void createCurrency_WhenNumericCodeExists_ShouldThrowException() {
        // Given
        when(currencyRepository.existsByCode("USD")).thenReturn(false);
        when(currencyRepository.existsByNumericCode("840")).thenReturn(true);

        // When & Then
        assertThrows(CurrencyAlreadyExistsException.class,
                () -> currencyService.createCurrency(requestDTO));
    }

    @Test
    void activateCurrency_WhenExists_ShouldActivateSuccessfully() {
        // Given
        currency.setIsActive(false);
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.save(currency)).thenReturn(currency);
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        // When
        CurrencyResponseDTO result = currencyService.activateCurrency(1L);

        // Then
        assertTrue(currency.getIsActive());
        verify(currencyRepository, times(1)).save(currency);
    }

    @Test
    void deactivateCurrency_WhenExists_ShouldDeactivateSuccessfully() {
        // Given
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.save(currency)).thenReturn(currency);
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        // When
        CurrencyResponseDTO result = currencyService.deactivateCurrency(1L);

        // Then
        assertFalse(currency.getIsActive());
        verify(currencyRepository, times(1)).save(currency);
    }
}
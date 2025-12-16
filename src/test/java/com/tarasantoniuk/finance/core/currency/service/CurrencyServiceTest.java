package com.tarasantoniuk.finance.core.currency.service;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyRequestDto;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyAlreadyExistsException;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.core.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    private CurrencyRequestDto requestDTO;
    private CurrencyResponseDto responseDTO;

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

        requestDTO = new CurrencyRequestDto();
        requestDTO.setCode("USD");
        requestDTO.setNumericCode("840");
        requestDTO.setName("US Dollar");
        requestDTO.setSymbol("$");
        requestDTO.setMinorUnit(2);

        responseDTO = new CurrencyResponseDto();
        responseDTO.setId(1L);
        responseDTO.setCode("USD");
        responseDTO.setNumericCode("840");
    }

    // ========== EXISTING TESTS ==========

    @Test
    void getCurrencyById_WhenExists_ShouldReturnCurrency() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        CurrencyResponseDto result = currencyService.getCurrencyById(1L);

        assertNotNull(result);
        assertEquals("USD", result.getCode());
    }

    @Test
    void getCurrencyById_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CurrencyNotFoundException.class,
                () -> currencyService.getCurrencyById(1L));
    }

    @Test
    void getCurrencyByCode_WhenExists_ShouldReturnCurrency() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(currency));
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        CurrencyResponseDto result = currencyService.getCurrencyByCode("USD");

        assertNotNull(result);
        assertEquals("USD", result.getCode());
    }

    @Test
    void createCurrency_WhenValid_ShouldReturnCreatedCurrency() {
        when(currencyRepository.existsByCode("USD")).thenReturn(false);
        when(currencyRepository.existsByNumericCode("840")).thenReturn(false);
        when(currencyMapper.toEntity(requestDTO)).thenReturn(currency);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        CurrencyResponseDto result = currencyService.createCurrency(requestDTO);

        assertNotNull(result);
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    void createCurrency_WhenCodeExists_ShouldThrowException() {
        when(currencyRepository.existsByCode("USD")).thenReturn(true);

        assertThrows(CurrencyAlreadyExistsException.class,
                () -> currencyService.createCurrency(requestDTO));
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void createCurrency_WhenNumericCodeExists_ShouldThrowException() {
        when(currencyRepository.existsByCode("USD")).thenReturn(false);
        when(currencyRepository.existsByNumericCode("840")).thenReturn(true);

        assertThrows(CurrencyAlreadyExistsException.class,
                () -> currencyService.createCurrency(requestDTO));
    }

    @Test
    void activateCurrency_WhenExists_ShouldActivateSuccessfully() {
        currency.setIsActive(false);
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.save(currency)).thenReturn(currency);
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        currencyService.activateCurrency(1L);


        assertTrue(currency.getIsActive());
        verify(currencyRepository, times(1)).save(currency);
    }

    @Test
    void deactivateCurrency_WhenExists_ShouldDeactivateSuccessfully() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.save(currency)).thenReturn(currency);
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        currencyService.deactivateCurrency(1L);

        assertFalse(currency.getIsActive());
        verify(currencyRepository, times(1)).save(currency);
    }

    // ========== NEW TESTS TO IMPROVE COVERAGE ==========

    @Test
    void getAllCurrencies_ShouldReturnAllCurrencies() {
        List<Currency> currencies = List.of(currency);
        List<CurrencyResponseDto> responseDTOs = List.of(responseDTO);

        when(currencyRepository.findAll()).thenReturn(currencies);
        when(currencyMapper.toResponseDTOList(currencies)).thenReturn(responseDTOs);

        List<CurrencyResponseDto> result = currencyService.getAllCurrencies();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(currencyRepository).findAll();
        verify(currencyMapper).toResponseDTOList(currencies);
    }

    @Test
    void getActiveCurrencies_ShouldReturnOnlyActiveCurrencies() {
        List<Currency> activeCurrencies = List.of(currency);
        List<CurrencyResponseDto> responseDTOs = List.of(responseDTO);

        when(currencyRepository.findByIsActive(true)).thenReturn(activeCurrencies);
        when(currencyMapper.toResponseDTOList(activeCurrencies)).thenReturn(responseDTOs);

        List<CurrencyResponseDto> result = currencyService.getActiveCurrencies();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(currencyRepository).findByIsActive(true);
        verify(currencyMapper).toResponseDTOList(activeCurrencies);
    }

    @Test
    void getCurrencyByCode_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.findByCode("XXX")).thenReturn(Optional.empty());

        assertThrows(CurrencyNotFoundException.class,
                () -> currencyService.getCurrencyByCode("XXX"));
    }

    @Test
    void getCurrencyByCode_ShouldConvertToUpperCase() {
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(currency));
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        CurrencyResponseDto result = currencyService.getCurrencyByCode("usd");

        assertNotNull(result);
        verify(currencyRepository).findByCode("USD");
    }

    @Test
    void getCurrencyByNumericCode_WhenExists_ShouldReturnCurrency() {
        when(currencyRepository.findByNumericCode("840")).thenReturn(Optional.of(currency));
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        CurrencyResponseDto result = currencyService.getCurrencyByNumericCode("840");

        assertNotNull(result);
        assertEquals("840", result.getNumericCode());
        verify(currencyRepository).findByNumericCode("840");
    }

    @Test
    void getCurrencyByNumericCode_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.findByNumericCode("999")).thenReturn(Optional.empty());

        assertThrows(CurrencyNotFoundException.class,
                () -> currencyService.getCurrencyByNumericCode("999"));
    }

    @Test
    void searchCurrenciesByName_ShouldReturnMatchingCurrencies() {
        List<Currency> currencies = List.of(currency);
        List<CurrencyResponseDto> responseDTOs = List.of(responseDTO);

        when(currencyRepository.findByNameContainingIgnoreCase("Dollar")).thenReturn(currencies);
        when(currencyMapper.toResponseDTOList(currencies)).thenReturn(responseDTOs);

        List<CurrencyResponseDto> result = currencyService.searchCurrenciesByName("Dollar");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(currencyRepository).findByNameContainingIgnoreCase("Dollar");
    }

    @Test
    void searchCurrenciesByName_WhenNoMatches_ShouldReturnEmptyList() {
        when(currencyRepository.findByNameContainingIgnoreCase("NonExistent")).thenReturn(List.of());
        when(currencyMapper.toResponseDTOList(List.of())).thenReturn(List.of());

        List<CurrencyResponseDto> result = currencyService.searchCurrenciesByName("NonExistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createCurrency_ShouldConvertCodeToUpperCase() {
        requestDTO.setCode("usd");
        when(currencyRepository.existsByCode("USD")).thenReturn(false);
        when(currencyRepository.existsByNumericCode("840")).thenReturn(false);
        when(currencyMapper.toEntity(requestDTO)).thenReturn(currency);
        when(currencyRepository.save(any(Currency.class))).thenReturn(currency);
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        CurrencyResponseDto result = currencyService.createCurrency(requestDTO);

        assertNotNull(result);
        verify(currencyRepository).existsByCode("USD");
    }

    @Test
    void updateCurrency_WhenValid_ShouldReturnUpdatedCurrency() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.save(currency)).thenReturn(currency);
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        CurrencyResponseDto result = currencyService.updateCurrency(1L, requestDTO);

        assertNotNull(result);
        verify(currencyMapper).updateEntityFromDTO(requestDTO, currency);
        verify(currencyRepository).save(currency);
    }

    @Test
    void updateCurrency_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CurrencyNotFoundException.class,
                () -> currencyService.updateCurrency(1L, requestDTO));
    }

    @Test
    void updateCurrency_WhenChangingToExistingCode_ShouldThrowException() {
        requestDTO.setCode("EUR");
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);

        assertThrows(CurrencyAlreadyExistsException.class,
                () -> currencyService.updateCurrency(1L, requestDTO));
    }

    @Test
    void updateCurrency_WhenChangingToExistingNumericCode_ShouldThrowException() {
        requestDTO.setCode("EUR");
        requestDTO.setNumericCode("978");
        Currency existingCurrency = new Currency();
        existingCurrency.setId(1L);
        existingCurrency.setCode("USD");
        existingCurrency.setNumericCode("840");

        when(currencyRepository.findById(1L)).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.existsByCode("EUR")).thenReturn(false);
        when(currencyRepository.existsByNumericCode("978")).thenReturn(true);

        assertThrows(CurrencyAlreadyExistsException.class,
                () -> currencyService.updateCurrency(1L, requestDTO));
    }

    @Test
    void updateCurrency_WhenNotChangingCode_ShouldNotCheckExistence() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency));
        when(currencyRepository.save(currency)).thenReturn(currency);
        when(currencyMapper.toResponseDTO(currency)).thenReturn(responseDTO);

        CurrencyResponseDto result = currencyService.updateCurrency(1L, requestDTO);

        assertNotNull(result);
        verify(currencyRepository, never()).existsByCode(anyString());
        verify(currencyRepository, never()).existsByNumericCode(anyString());
    }

    @Test
    void updateCurrency_ShouldConvertCodeToUpperCase() {
        requestDTO.setCode("eur");
        Currency currency2 = new Currency();
        currency2.setId(1L);
        currency2.setCode("USD");
        currency2.setNumericCode("840");

        when(currencyRepository.findById(1L)).thenReturn(Optional.of(currency2));
        when(currencyRepository.existsByCode("EUR")).thenReturn(false);
        when(currencyRepository.save(currency2)).thenReturn(currency2);
        when(currencyMapper.toResponseDTO(currency2)).thenReturn(responseDTO);

        CurrencyResponseDto result = currencyService.updateCurrency(1L, requestDTO);

        assertNotNull(result);
        verify(currencyRepository).existsByCode("EUR");
    }

    @Test
    void deleteCurrency_WhenExists_ShouldDeleteSuccessfully() {
        when(currencyRepository.existsById(1L)).thenReturn(true);

        currencyService.deleteCurrency(1L);

        verify(currencyRepository).deleteById(1L);
    }

    @Test
    void deleteCurrency_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.existsById(1L)).thenReturn(false);

        assertThrows(CurrencyNotFoundException.class,
                () -> currencyService.deleteCurrency(1L));
        verify(currencyRepository, never()).deleteById(anyLong());
    }

    @Test
    void deactivateCurrency_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CurrencyNotFoundException.class,
                () -> currencyService.deactivateCurrency(1L));
    }

    @Test
    void activateCurrency_WhenNotExists_ShouldThrowException() {
        when(currencyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CurrencyNotFoundException.class,
                () -> currencyService.activateCurrency(1L));
    }
}
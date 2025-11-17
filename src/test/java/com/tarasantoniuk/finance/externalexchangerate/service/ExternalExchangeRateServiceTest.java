package com.tarasantoniuk.finance.externalexchangerate.service;

import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.exception.CurrencyNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    // ========== BASIC CRUD TESTS ==========

    @Test
    void getExchangeRateById_WhenExists_ShouldReturnRate() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        ExternalExchangeRateResponseDTO result = exchangeRateService.getExchangeRateById(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(0.92), result.getRate());
    }

    @Test
    void getExchangeRateById_WhenNotExists_ShouldThrowException() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.getExchangeRateById(1L));
    }

    @Test
    void getAllExchangeRates_ShouldReturnAllRates() {
        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        List<ExternalExchangeRateResponseDTO> responseDTOs = List.of(responseDTO);

        when(exchangeRateRepository.findAll()).thenReturn(rates);
        when(exchangeRateMapper.toResponseDTOList(rates)).thenReturn(responseDTOs);

        List<ExternalExchangeRateResponseDTO> result = exchangeRateService.getAllExchangeRates();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exchangeRateRepository).findAll();
    }

    // ========== CREATE TESTS ==========

    @Test
    void createExchangeRate_WhenValid_ShouldReturnCreatedRate() {
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(true);
        when(exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                any(), anyLong(), anyLong(), anyString())).thenReturn(false);
        when(exchangeRateMapper.toEntity(requestDTO)).thenReturn(exchangeRate);
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        ExternalExchangeRateResponseDTO result = exchangeRateService.createExchangeRate(requestDTO);

        assertNotNull(result);
        verify(exchangeRateRepository, times(1)).save(any(ExternalExchangeRate.class));
    }

    @Test
    void createExchangeRate_WhenCurrencyFromNotExists_ShouldThrowException() {
        when(currencyRepository.existsById(1L)).thenReturn(false);

        assertThrows(CurrencyNotFoundException.class,
                () -> exchangeRateService.createExchangeRate(requestDTO));
    }

    @Test
    void createExchangeRate_WhenCurrencyToNotExists_ShouldThrowException() {
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(false);

        assertThrows(CurrencyNotFoundException.class,
                () -> exchangeRateService.createExchangeRate(requestDTO));
    }

    @Test
    void createExchangeRate_WhenSameCurrency_ShouldThrowException() {
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(1L);
        when(currencyRepository.existsById(1L)).thenReturn(true);

        assertThrows(InvalidExchangeRateException.class,
                () -> exchangeRateService.createExchangeRate(requestDTO));
        verify(exchangeRateRepository, never()).save(any(ExternalExchangeRate.class));
    }

    @Test
    void createExchangeRate_WhenAlreadyExists_ShouldThrowException() {
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(true);
        when(exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                any(), anyLong(), anyLong(), anyString())).thenReturn(true);

        assertThrows(ExchangeRateAlreadyExistsException.class,
                () -> exchangeRateService.createExchangeRate(requestDTO));
    }

    // ========== UPDATE TESTS - COMPREHENSIVE BRANCH COVERAGE ==========

    @Test
    void updateExchangeRate_WhenValid_ShouldReturnUpdatedRate() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(true);
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        ExternalExchangeRateResponseDTO result = exchangeRateService.updateExchangeRate(1L, requestDTO);

        assertNotNull(result);
        verify(exchangeRateMapper).updateEntityFromDTO(requestDTO, exchangeRate);
        verify(exchangeRateRepository).save(exchangeRate);
    }

    @Test
    void updateExchangeRate_WhenNotExists_ShouldThrowException() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.updateExchangeRate(1L, requestDTO));
    }

    @Test
    void updateExchangeRate_WhenCurrencyFromNotExists_ShouldThrowException() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(1L)).thenReturn(false);

        assertThrows(CurrencyNotFoundException.class,
                () -> exchangeRateService.updateExchangeRate(1L, requestDTO));
    }

    @Test
    void updateExchangeRate_WhenCurrencyToNotExists_ShouldThrowException() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(false);

        assertThrows(CurrencyNotFoundException.class,
                () -> exchangeRateService.updateExchangeRate(1L, requestDTO));
    }

    @Test
    void updateExchangeRate_WhenSameCurrency_ShouldThrowException() {
        requestDTO.setCurrencyToId(1L);
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(1L)).thenReturn(true);

        assertThrows(InvalidExchangeRateException.class,
                () -> exchangeRateService.updateExchangeRate(1L, requestDTO));
    }

    @Test
    void updateExchangeRate_WhenChangingOnlyDate_ShouldCheckForDuplicate() {
        // Given - changing only date
        exchangeRate.setExchangeDate(LocalDate.of(2024, 1, 1));
        exchangeRate.getCurrencyFrom().setId(1L);
        exchangeRate.getCurrencyTo().setId(2L);
        exchangeRate.setSource("ECB");

        requestDTO.setExchangeDate(LocalDate.of(2024, 2, 1)); // New date
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setSource("ECB");

        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(true);
        when(exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                LocalDate.of(2024, 2, 1), 1L, 2L, "ECB")).thenReturn(true);

        // When & Then
        assertThrows(ExchangeRateAlreadyExistsException.class,
                () -> exchangeRateService.updateExchangeRate(1L, requestDTO));
    }

    @Test
    void updateExchangeRate_WhenChangingOnlyCurrencyFrom_ShouldCheckForDuplicate() {
        // Given - changing only currencyFrom
        LocalDate date = LocalDate.of(2024, 1, 1);
        exchangeRate.setExchangeDate(date);
        exchangeRate.getCurrencyFrom().setId(1L);
        exchangeRate.getCurrencyTo().setId(2L);
        exchangeRate.setSource("ECB");

        requestDTO.setExchangeDate(date);
        requestDTO.setCurrencyFromId(3L); // New currency
        requestDTO.setCurrencyToId(2L);
        requestDTO.setSource("ECB");

        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(3L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(true);
        when(exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                date, 3L, 2L, "ECB")).thenReturn(true);

        // When & Then
        assertThrows(ExchangeRateAlreadyExistsException.class,
                () -> exchangeRateService.updateExchangeRate(1L, requestDTO));
    }

    @Test
    void updateExchangeRate_WhenChangingOnlyCurrencyTo_ShouldCheckForDuplicate() {
        // Given - changing only currencyTo
        LocalDate date = LocalDate.of(2024, 1, 1);
        exchangeRate.setExchangeDate(date);
        exchangeRate.getCurrencyFrom().setId(1L);
        exchangeRate.getCurrencyTo().setId(2L);
        exchangeRate.setSource("ECB");

        requestDTO.setExchangeDate(date);
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(3L); // New currency
        requestDTO.setSource("ECB");

        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(3L)).thenReturn(true);
        when(exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                date, 1L, 3L, "ECB")).thenReturn(true);

        // When & Then
        assertThrows(ExchangeRateAlreadyExistsException.class,
                () -> exchangeRateService.updateExchangeRate(1L, requestDTO));
    }

    @Test
    void updateExchangeRate_WhenChangingOnlySource_ShouldCheckForDuplicate() {
        // Given - changing only source
        LocalDate date = LocalDate.of(2024, 1, 1);
        exchangeRate.setExchangeDate(date);
        exchangeRate.getCurrencyFrom().setId(1L);
        exchangeRate.getCurrencyTo().setId(2L);
        exchangeRate.setSource("ECB");

        requestDTO.setExchangeDate(date);
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setSource("NBU"); // New source

        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(true);
        when(exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                date, 1L, 2L, "NBU")).thenReturn(true);

        // When & Then
        assertThrows(ExchangeRateAlreadyExistsException.class,
                () -> exchangeRateService.updateExchangeRate(1L, requestDTO));
    }

    @Test
    void updateExchangeRate_WhenNotChangingAnything_ShouldNotCheckForDuplicate() {
        // Given - not changing any key fields
        LocalDate date = LocalDate.of(2024, 1, 1);
        exchangeRate.setExchangeDate(date);
        exchangeRate.getCurrencyFrom().setId(1L);
        exchangeRate.getCurrencyTo().setId(2L);
        exchangeRate.setSource("ECB");

        requestDTO.setExchangeDate(date);
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setSource("ECB");
        requestDTO.setRate(BigDecimal.valueOf(0.95)); // Changing only rate

        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(2L)).thenReturn(true);
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        // When
        ExternalExchangeRateResponseDTO result = exchangeRateService.updateExchangeRate(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(exchangeRateRepository, never()).existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                any(), anyLong(), anyLong(), anyString());
        verify(exchangeRateRepository).save(exchangeRate);
    }

    @Test
    void updateExchangeRate_WhenChangingMultipleFieldsToNonExisting_ShouldSucceed() {
        // Given - changing multiple fields but combination doesn't exist
        LocalDate oldDate = LocalDate.of(2024, 1, 1);
        LocalDate newDate = LocalDate.of(2024, 2, 1);

        exchangeRate.setExchangeDate(oldDate);
        exchangeRate.getCurrencyFrom().setId(1L);
        exchangeRate.getCurrencyTo().setId(2L);
        exchangeRate.setSource("ECB");

        requestDTO.setExchangeDate(newDate);
        requestDTO.setCurrencyFromId(3L);
        requestDTO.setCurrencyToId(4L);
        requestDTO.setSource("NBU");

        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(currencyRepository.existsById(3L)).thenReturn(true);
        when(currencyRepository.existsById(4L)).thenReturn(true);
        when(exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                newDate, 3L, 4L, "NBU")).thenReturn(false); // Doesn't exist
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        // When
        ExternalExchangeRateResponseDTO result = exchangeRateService.updateExchangeRate(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(exchangeRateRepository).existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                newDate, 3L, 4L, "NBU");
        verify(exchangeRateRepository).save(exchangeRate);
    }

    // ========== DELETE & ACTIVATION TESTS ==========

    @Test
    void deleteExchangeRate_WhenExists_ShouldDeleteSuccessfully() {
        when(exchangeRateRepository.existsById(1L)).thenReturn(true);

        exchangeRateService.deleteExchangeRate(1L);

        verify(exchangeRateRepository).deleteById(1L);
    }

    @Test
    void deleteExchangeRate_WhenNotExists_ShouldThrowException() {
        when(exchangeRateRepository.existsById(1L)).thenReturn(false);

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.deleteExchangeRate(1L));
    }

    @Test
    void activateExchangeRate_WhenExists_ShouldActivateSuccessfully() {
        exchangeRate.setIsActive(false);
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        ExternalExchangeRateResponseDTO result = exchangeRateService.activateExchangeRate(1L);

        assertTrue(exchangeRate.getIsActive());
        verify(exchangeRateRepository, times(1)).save(exchangeRate);
    }

    @Test
    void activateExchangeRate_WhenNotExists_ShouldThrowException() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.activateExchangeRate(1L));
    }

    @Test
    void deactivateExchangeRate_WhenExists_ShouldDeactivateSuccessfully() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        ExternalExchangeRateResponseDTO result = exchangeRateService.deactivateExchangeRate(1L);

        assertFalse(exchangeRate.getIsActive());
        verify(exchangeRateRepository).save(exchangeRate);
    }

    @Test
    void deactivateExchangeRate_WhenNotExists_ShouldThrowException() {
        when(exchangeRateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.deactivateExchangeRate(1L));
    }

    // ========== QUERY TESTS ==========

    @Test
    void getExchangeRatesByDate_ShouldReturnFilteredRates() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        List<ExternalExchangeRateResponseDTO> responseDTOs = List.of(responseDTO);

        when(exchangeRateRepository.findByExchangeDate(date)).thenReturn(rates);
        when(exchangeRateMapper.toResponseDTOList(rates)).thenReturn(responseDTOs);

        List<ExternalExchangeRateResponseDTO> result = exchangeRateService.getExchangeRatesByDate(date);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exchangeRateRepository).findByExchangeDate(date);
    }

    @Test
    void getExchangeRatesByDateAndSource_ShouldReturnFilteredRates() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        String source = "ECB";
        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        List<ExternalExchangeRateResponseDTO> responseDTOs = List.of(responseDTO);

        when(exchangeRateRepository.findByExchangeDateAndSource(date, source)).thenReturn(rates);
        when(exchangeRateMapper.toResponseDTOList(rates)).thenReturn(responseDTOs);

        List<ExternalExchangeRateResponseDTO> result = exchangeRateService.getExchangeRatesByDateAndSource(date, source);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exchangeRateRepository).findByExchangeDateAndSource(date, source);
    }

    @Test
    void getExchangeRatesByCurrencyPair_ShouldReturnFilteredRates() {
        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        List<ExternalExchangeRateResponseDTO> responseDTOs = List.of(responseDTO);

        when(exchangeRateRepository.findByCurrencyFromIdAndCurrencyToId(1L, 2L)).thenReturn(rates);
        when(exchangeRateMapper.toResponseDTOList(rates)).thenReturn(responseDTOs);

        List<ExternalExchangeRateResponseDTO> result = exchangeRateService.getExchangeRatesByCurrencyPair(1L, 2L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exchangeRateRepository).findByCurrencyFromIdAndCurrencyToId(1L, 2L);
    }

    @Test
    void getExchangeRatesBySource_ShouldReturnFilteredRates() {
        String source = "NBU";
        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        List<ExternalExchangeRateResponseDTO> responseDTOs = List.of(responseDTO);

        when(exchangeRateRepository.findBySource(source)).thenReturn(rates);
        when(exchangeRateMapper.toResponseDTOList(rates)).thenReturn(responseDTOs);

        List<ExternalExchangeRateResponseDTO> result = exchangeRateService.getExchangeRatesBySource(source);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exchangeRateRepository).findBySource(source);
    }

    @Test
    void getExchangeRatesByDateRange_ShouldReturnFilteredRates() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        List<ExternalExchangeRateResponseDTO> responseDTOs = List.of(responseDTO);

        when(exchangeRateRepository.findByExchangeDateBetween(startDate, endDate)).thenReturn(rates);
        when(exchangeRateMapper.toResponseDTOList(rates)).thenReturn(responseDTOs);

        List<ExternalExchangeRateResponseDTO> result = exchangeRateService.getExchangeRatesByDateRange(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exchangeRateRepository).findByExchangeDateBetween(startDate, endDate);
    }

    @Test
    void getExchangeRatesByDateRangeAndCurrencyPair_ShouldReturnFilteredRates() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        List<ExternalExchangeRateResponseDTO> responseDTOs = List.of(responseDTO);

        when(exchangeRateRepository.findByExchangeDateBetweenAndCurrencyFromIdAndCurrencyToId(
                startDate, endDate, 1L, 2L)).thenReturn(rates);
        when(exchangeRateMapper.toResponseDTOList(rates)).thenReturn(responseDTOs);

        List<ExternalExchangeRateResponseDTO> result =
                exchangeRateService.getExchangeRatesByDateRangeAndCurrencyPair(startDate, endDate, 1L, 2L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exchangeRateRepository).findByExchangeDateBetweenAndCurrencyFromIdAndCurrencyToId(
                startDate, endDate, 1L, 2L);
    }

    @Test
    void getLatestRatesByDateAndCurrencyFrom_WhenExists_ShouldReturnRates() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        Long currencyFromId = 2L;

        ExternalExchangeRate rate1 = new ExternalExchangeRate();
        ExternalExchangeRate rate2 = new ExternalExchangeRate();

        ExternalExchangeRateResponseDTO responseDTO1 = new ExternalExchangeRateResponseDTO();
        ExternalExchangeRateResponseDTO responseDTO2 = new ExternalExchangeRateResponseDTO();

        when(currencyRepository.existsById(currencyFromId)).thenReturn(true);
        when(exchangeRateRepository.findLatestRatesByCurrencyFromBeforeDate(date, currencyFromId))
                .thenReturn(List.of(rate1, rate2));
        when(exchangeRateMapper.toResponseDTOList(List.of(rate1, rate2)))
                .thenReturn(List.of(responseDTO1, responseDTO2));

        List<ExternalExchangeRateResponseDTO> result =
                exchangeRateService.getLatestRatesByDateAndCurrencyFrom(date, currencyFromId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(currencyRepository).existsById(currencyFromId);
        verify(exchangeRateRepository).findLatestRatesByCurrencyFromBeforeDate(date, currencyFromId);
        verify(exchangeRateMapper).toResponseDTOList(List.of(rate1, rate2));
    }

    @Test
    void getLatestRatesByDateAndCurrencyFrom_WhenCurrencyNotExists_ShouldThrowException() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        Long currencyFromId = 999L;

        when(currencyRepository.existsById(currencyFromId)).thenReturn(false);

        assertThrows(CurrencyNotFoundException.class,
                () -> exchangeRateService.getLatestRatesByDateAndCurrencyFrom(date, currencyFromId));

        verify(currencyRepository).existsById(currencyFromId);
        verify(exchangeRateRepository, never()).findLatestRatesByCurrencyFromBeforeDate(any(), any());
    }

    @Test
    void getLatestRatesByDateAndCurrencyFrom_WhenNoRatesFound_ShouldReturnEmptyList() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        Long currencyFromId = 2L;

        when(currencyRepository.existsById(currencyFromId)).thenReturn(true);
        when(exchangeRateRepository.findLatestRatesByCurrencyFromBeforeDate(date, currencyFromId))
                .thenReturn(List.of());
        when(exchangeRateMapper.toResponseDTOList(List.of())).thenReturn(List.of());

        List<ExternalExchangeRateResponseDTO> result =
                exchangeRateService.getLatestRatesByDateAndCurrencyFrom(date, currencyFromId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(exchangeRateRepository).findLatestRatesByCurrencyFromBeforeDate(date, currencyFromId);
    }

    // ========== CROSS RATE TESTS ==========

    @Test
    void calculateCrossRate_WhenRatesExist_ShouldReturnCalculatedRate() {
        LocalDate date = LocalDate.now();
        ExternalExchangeRate rate1 = new ExternalExchangeRate();
        rate1.setRate(BigDecimal.valueOf(1.2));

        ExternalExchangeRate rate2 = new ExternalExchangeRate();
        rate2.setRate(BigDecimal.valueOf(0.9));

        when(exchangeRateRepository.findLatestRateBeforeDate(date, 1L, 3L))
                .thenReturn(List.of(rate1));
        when(exchangeRateRepository.findLatestRateBeforeDate(date, 3L, 2L))
                .thenReturn(List.of(rate2));

        BigDecimal result = exchangeRateService.calculateCrossRate(1L, 2L, 3L, date);

        assertNotNull(result);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(1.08)));
    }

    @Test
    void calculateCrossRate_WhenFirstRateMissing_ShouldThrowException() {
        LocalDate date = LocalDate.now();
        when(exchangeRateRepository.findLatestRateBeforeDate(date, 1L, 3L))
                .thenReturn(List.of());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.calculateCrossRate(1L, 2L, 3L, date));
    }

    @Test
    void calculateCrossRate_WhenSecondRateMissing_ShouldThrowException() {
        LocalDate date = LocalDate.now();
        ExternalExchangeRate rate1 = new ExternalExchangeRate();
        rate1.setRate(BigDecimal.valueOf(1.2));

        when(exchangeRateRepository.findLatestRateBeforeDate(date, 1L, 3L))
                .thenReturn(List.of(rate1));
        when(exchangeRateRepository.findLatestRateBeforeDate(date, 3L, 2L))
                .thenReturn(List.of());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.calculateCrossRate(1L, 2L, 3L, date));
    }
}
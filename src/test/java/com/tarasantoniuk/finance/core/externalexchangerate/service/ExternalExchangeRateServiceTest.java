package com.tarasantoniuk.finance.core.externalexchangerate.service;

import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateRequestDTO;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateResponseDTO;
import com.tarasantoniuk.finance.core.externalexchangerate.entity.ExternalExchangeRate;
import com.tarasantoniuk.finance.core.externalexchangerate.exception.ExchangeRateAlreadyExistsException;
import com.tarasantoniuk.finance.core.externalexchangerate.exception.ExchangeRateNotFoundException;
import com.tarasantoniuk.finance.core.externalexchangerate.exception.InvalidExchangeRateException;
import com.tarasantoniuk.finance.core.externalexchangerate.mapper.ExternalExchangeRateMapper;
import com.tarasantoniuk.finance.core.externalexchangerate.repository.ExternalExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
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
        when(exchangeRateRepository.findByIdWithCurrencies(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        ExternalExchangeRateResponseDTO result = exchangeRateService.getExchangeRateById(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(0.92), result.getRate());
    }

    @Test
    void getExchangeRateById_WhenNotExists_ShouldThrowException() {
        when(exchangeRateRepository.findByIdWithCurrencies(1L)).thenReturn(Optional.empty());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.getExchangeRateById(1L));
    }

    @Test
    void getAllExchangeRates_ShouldReturnPagedRates() {
        int page = 0;
        int size = 200;

        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        Page<ExternalExchangeRate> ratePage = new PageImpl<>(rates,
                PageRequest.of(page, size), 1);

        when(exchangeRateRepository.findAllWithCurrencies(any(Pageable.class))).thenReturn(ratePage);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        PageResponse<ExternalExchangeRateResponseDTO> result =
                exchangeRateService.getAllExchangeRates(page, size);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());

        PageMetadata metadata = result.getMetadata();
        assertEquals(0, metadata.getCurrentPage());
        assertEquals(1, metadata.getTotalPages());
        assertEquals(200, metadata.getPageSize());
        assertEquals(1, metadata.getTotalElements());
        assertFalse(metadata.isHasNext());
        assertFalse(metadata.isHasPrevious());

        verify(exchangeRateRepository).findAllWithCurrencies(any(Pageable.class));
    }

    @Test
    void getAllExchangeRates_WithMultiplePages_ShouldReturnCorrectMetadata() {
        int page = 1;
        int size = 200;

        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        Page<ExternalExchangeRate> ratePage = new PageImpl<>(rates,
                PageRequest.of(page, size), 450);

        when(exchangeRateRepository.findAllWithCurrencies(any(Pageable.class))).thenReturn(ratePage);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        PageResponse<ExternalExchangeRateResponseDTO> result =
                exchangeRateService.getAllExchangeRates(page, size);

        assertNotNull(result);

        PageMetadata metadata = result.getMetadata();
        assertEquals(1, metadata.getCurrentPage());
        assertEquals(3, metadata.getTotalPages());
        assertEquals(200, metadata.getPageSize());
        assertEquals(450, metadata.getTotalElements());
        assertTrue(metadata.isHasNext());
        assertTrue(metadata.isHasPrevious());
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

    // ========== UPDATE TESTS ==========

    @Test
    void updateExchangeRate_WhenValid_ShouldReturnUpdatedRate() {
        when(exchangeRateRepository.findByIdWithCurrencies(1L)).thenReturn(Optional.of(exchangeRate));
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
        when(exchangeRateRepository.findByIdWithCurrencies(1L)).thenReturn(Optional.empty());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.updateExchangeRate(1L, requestDTO));
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
        when(exchangeRateRepository.findByIdWithCurrencies(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        ExternalExchangeRateResponseDTO result = exchangeRateService.activateExchangeRate(1L);

        assertTrue(exchangeRate.getIsActive());
        verify(exchangeRateRepository, times(1)).save(exchangeRate);
    }

    @Test
    void deactivateExchangeRate_WhenExists_ShouldDeactivateSuccessfully() {
        when(exchangeRateRepository.findByIdWithCurrencies(1L)).thenReturn(Optional.of(exchangeRate));
        when(exchangeRateRepository.save(exchangeRate)).thenReturn(exchangeRate);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        ExternalExchangeRateResponseDTO result = exchangeRateService.deactivateExchangeRate(1L);

        assertFalse(exchangeRate.getIsActive());
        verify(exchangeRateRepository).save(exchangeRate);
    }

    // ========== QUERY TESTS ==========

    @Test
    void getExchangeRatesByDate_ShouldReturnFilteredRates() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        List<ExternalExchangeRateResponseDTO> responseDTOs = List.of(responseDTO);

        when(exchangeRateRepository.findByExchangeDateWithCurrencies(date)).thenReturn(rates);
        when(exchangeRateMapper.toResponseDTOList(rates)).thenReturn(responseDTOs);

        List<ExternalExchangeRateResponseDTO> result = exchangeRateService.getExchangeRatesByDate(date);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exchangeRateRepository).findByExchangeDateWithCurrencies(date);
    }

    @Test
    void getExchangeRatesByDateAndSource_ShouldReturnFilteredRates() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        String source = "ECB";
        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        List<ExternalExchangeRateResponseDTO> responseDTOs = List.of(responseDTO);

        when(exchangeRateRepository.findByExchangeDateAndSourceWithCurrencies(date, source)).thenReturn(rates);
        when(exchangeRateMapper.toResponseDTOList(rates)).thenReturn(responseDTOs);

        List<ExternalExchangeRateResponseDTO> result = exchangeRateService.getExchangeRatesByDateAndSource(date, source);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(exchangeRateRepository).findByExchangeDateAndSourceWithCurrencies(date, source);
    }

    @Test
    void getExchangeRatesByCurrencyPair_ShouldReturnPagedRates() {
        int page = 0;
        int size = 200;

        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        Page<ExternalExchangeRate> ratePage = new PageImpl<>(rates,
                PageRequest.of(page, size), 1);

        when(exchangeRateRepository.findByCurrencyPairWithCurrencies(eq(1L), eq(2L), any(Pageable.class)))
                .thenReturn(ratePage);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        PageResponse<ExternalExchangeRateResponseDTO> result =
                exchangeRateService.getExchangeRatesByCurrencyPair(1L, 2L, page, size);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getExchangeRatesByDateRange_ShouldReturnPagedRates() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        int page = 0;
        int size = 200;

        List<ExternalExchangeRate> rates = List.of(exchangeRate);
        Page<ExternalExchangeRate> ratePage = new PageImpl<>(rates,
                PageRequest.of(page, size), 1);

        when(exchangeRateRepository.findByExchangeDateBetweenWithCurrencies(eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(ratePage);
        when(exchangeRateMapper.toResponseDTO(exchangeRate)).thenReturn(responseDTO);

        PageResponse<ExternalExchangeRateResponseDTO> result =
                exchangeRateService.getExchangeRatesByDateRange(startDate, endDate, page, size);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
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
        when(exchangeRateRepository.findLatestRatesByCurrencyFromWithCurrencies(date, currencyFromId))
                .thenReturn(List.of(rate1, rate2));
        when(exchangeRateMapper.toResponseDTOList(List.of(rate1, rate2)))
                .thenReturn(List.of(responseDTO1, responseDTO2));

        List<ExternalExchangeRateResponseDTO> result =
                exchangeRateService.getLatestRatesByDateAndCurrencyFrom(date, currencyFromId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ========== CROSS RATE TESTS ==========

    @Test
    void calculateCrossRate_WhenRatesExist_ShouldReturnCalculatedRate() {
        LocalDate date = LocalDate.now();
        ExternalExchangeRate rate1 = new ExternalExchangeRate();
        rate1.setRate(BigDecimal.valueOf(1.2));

        ExternalExchangeRate rate2 = new ExternalExchangeRate();
        rate2.setRate(BigDecimal.valueOf(0.9));

        when(exchangeRateRepository.findLatestRateBeforeDateWithCurrencies(date, 1L, 3L))
                .thenReturn(List.of(rate1));
        when(exchangeRateRepository.findLatestRateBeforeDateWithCurrencies(date, 3L, 2L))
                .thenReturn(List.of(rate2));

        BigDecimal result = exchangeRateService.calculateCrossRate(1L, 2L, 3L, date);

        assertNotNull(result);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(1.08)));
    }

    @Test
    void calculateCrossRate_WhenFirstRateMissing_ShouldThrowException() {
        LocalDate date = LocalDate.now();
        when(exchangeRateRepository.findLatestRateBeforeDateWithCurrencies(date, 1L, 3L))
                .thenReturn(List.of());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.calculateCrossRate(1L, 2L, 3L, date));
    }

    @Test
    void calculateCrossRate_WhenSecondRateMissing_ShouldThrowException() {
        LocalDate date = LocalDate.now();
        ExternalExchangeRate rate1 = new ExternalExchangeRate();
        rate1.setRate(BigDecimal.valueOf(1.2));

        when(exchangeRateRepository.findLatestRateBeforeDateWithCurrencies(date, 1L, 3L))
                .thenReturn(List.of(rate1));
        when(exchangeRateRepository.findLatestRateBeforeDateWithCurrencies(date, 3L, 2L))
                .thenReturn(List.of());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.calculateCrossRate(1L, 2L, 3L, date));
    }
}
package com.tarasantoniuk.finance.core.externalexchangerate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateRequestDto;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateResponseDto;
import com.tarasantoniuk.finance.core.externalexchangerate.service.ExternalExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExternalExchangeRateController.class)
class ExternalExchangeRateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExternalExchangeRateService exchangeRateService;

    @Test
    void getAllExchangeRates_WithDefaultParameters_ShouldReturnPagedRates() throws Exception {
        // Given
        ExternalExchangeRateResponseDto rate1 = new ExternalExchangeRateResponseDto();
        rate1.setId(1L);
        rate1.setExchangeDate(LocalDate.now());
        rate1.setRate(BigDecimal.valueOf(0.92));

        ExternalExchangeRateResponseDto rate2 = new ExternalExchangeRateResponseDto();
        rate2.setId(2L);
        rate2.setExchangeDate(LocalDate.now().minusDays(1));
        rate2.setRate(BigDecimal.valueOf(38.50));

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(1)
                .pageSize(200)
                .totalElements(2)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of(rate1, rate2))
                        .metadata(metadata)
                        .build();

        when(exchangeRateService.getAllExchangeRates(0, 200)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].rate").value(0.92))
                .andExpect(jsonPath("$.content[1].rate").value(38.50))
                .andExpect(jsonPath("$.metadata.currentPage").value(0))
                .andExpect(jsonPath("$.metadata.totalPages").value(1))
                .andExpect(jsonPath("$.metadata.pageSize").value(200))
                .andExpect(jsonPath("$.metadata.totalElements").value(2))
                .andExpect(jsonPath("$.metadata.hasNext").value(false))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(false));
    }

    @Test
    void getAllExchangeRates_WithCustomPageAndSize_ShouldReturnPagedRates() throws Exception {
        // Given
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);
        rate.setRate(BigDecimal.valueOf(0.92));

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(1)
                .totalPages(3)
                .pageSize(100)
                .totalElements(250)
                .hasNext(true)
                .hasPrevious(true)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of(rate))
                        .metadata(metadata)
                        .build();

        when(exchangeRateService.getAllExchangeRates(1, 100)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates")
                        .param("page", "1")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.metadata.currentPage").value(1))
                .andExpect(jsonPath("$.metadata.totalPages").value(3))
                .andExpect(jsonPath("$.metadata.pageSize").value(100))
                .andExpect(jsonPath("$.metadata.totalElements").value(250))
                .andExpect(jsonPath("$.metadata.hasNext").value(true))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(true));
    }

    @Test
    void getAllExchangeRates_WhenSizeExceedsMaxSize_ShouldLimitToMaxSize() throws Exception {
        // Given
        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(1)
                .pageSize(500)
                .totalElements(100)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of())
                        .metadata(metadata)
                        .build();

        // Requesting 1000 but should be limited to 500 (maxSize)
        when(exchangeRateService.getAllExchangeRates(0, 500)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.pageSize").value(500));
    }

    @Test
    void getAllExchangeRates_WithEmptyResults_ShouldReturnEmptyPage() throws Exception {
        // Given
        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(0)
                .pageSize(200)
                .totalElements(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of())
                        .metadata(metadata)
                        .build();

        when(exchangeRateService.getAllExchangeRates(0, 200)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.metadata.totalElements").value(0));
    }

    @Test
    void getExchangeRateById_WhenExists_ShouldReturnRate() throws Exception {
        // Given
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);
        rate.setExchangeDate(LocalDate.now());
        rate.setRate(BigDecimal.valueOf(0.92));

        when(exchangeRateService.getExchangeRateById(1L)).thenReturn(rate);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rate").value(0.92));
    }

    @Test
    void getExchangeRatesByDate_ShouldReturnFilteredList() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);
        rate.setExchangeDate(date);

        when(exchangeRateService.getExchangeRatesByDate(date))
                .thenReturn(List.of(rate));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/date/2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getExchangeRatesByDateAndSource_ShouldReturnFilteredList() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);
        rate.setExchangeDate(date);

        when(exchangeRateService.getExchangeRatesByDateAndSource(date, "ECB"))
                .thenReturn(List.of(rate));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/date/2024-01-15/source/ECB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getExchangeRatesByCurrencyPair_WithDefaultParameters_ShouldReturnPagedRates() throws Exception {
        // Given
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);
        rate.setRate(BigDecimal.valueOf(0.92));

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(1)
                .pageSize(200)
                .totalElements(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of(rate))
                        .metadata(metadata)
                        .build();

        when(exchangeRateService.getExchangeRatesByCurrencyPair(1L, 2L, 0, 200))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/currency-pair")
                        .param("currencyFromId", "1")
                        .param("currencyToId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].rate").value(0.92))
                .andExpect(jsonPath("$.metadata.currentPage").value(0))
                .andExpect(jsonPath("$.metadata.totalPages").value(1))
                .andExpect(jsonPath("$.metadata.pageSize").value(200))
                .andExpect(jsonPath("$.metadata.totalElements").value(1));
    }

    @Test
    void getExchangeRatesByCurrencyPair_WithCustomPageAndSize_ShouldReturnPagedRates() throws Exception {
        // Given
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);
        rate.setRate(BigDecimal.valueOf(0.92));

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(1)
                .totalPages(5)
                .pageSize(100)
                .totalElements(450)
                .hasNext(true)
                .hasPrevious(true)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of(rate))
                        .metadata(metadata)
                        .build();

        when(exchangeRateService.getExchangeRatesByCurrencyPair(1L, 2L, 1, 100))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/currency-pair")
                        .param("currencyFromId", "1")
                        .param("currencyToId", "2")
                        .param("page", "1")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.metadata.currentPage").value(1))
                .andExpect(jsonPath("$.metadata.totalPages").value(5))
                .andExpect(jsonPath("$.metadata.hasNext").value(true))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(true));
    }

    @Test
    void getExchangeRatesByCurrencyPair_WhenSizeExceedsMaxSize_ShouldLimitToMaxSize() throws Exception {
        // Given
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);
        rate.setRate(BigDecimal.valueOf(0.92));

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(1)
                .pageSize(500)
                .totalElements(100)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of(rate))
                        .metadata(metadata)
                        .build();

        // Requesting 1000 but should be limited to 500 (maxSize)
        when(exchangeRateService.getExchangeRatesByCurrencyPair(1L, 2L, 0, 500))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/currency-pair")
                        .param("currencyFromId", "1")
                        .param("currencyToId", "2")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.pageSize").value(500));
    }

    @Test
    void getExchangeRatesByDateRange_WithDefaultParameters_ShouldReturnPagedRates() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(1)
                .pageSize(200)
                .totalElements(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of(rate))
                        .metadata(metadata)
                        .build();

        when(exchangeRateService.getExchangeRatesByDateRange(startDate, endDate, 0, 200))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.metadata.currentPage").value(0))
                .andExpect(jsonPath("$.metadata.totalPages").value(1))
                .andExpect(jsonPath("$.metadata.pageSize").value(200))
                .andExpect(jsonPath("$.metadata.totalElements").value(1));
    }

    @Test
    void getExchangeRatesByDateRange_WithCustomPageAndSize_ShouldReturnPagedRates() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(2)
                .totalPages(10)
                .pageSize(50)
                .totalElements(500)
                .hasNext(true)
                .hasPrevious(true)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of(rate))
                        .metadata(metadata)
                        .build();

        when(exchangeRateService.getExchangeRatesByDateRange(startDate, endDate, 2, 50))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .param("page", "2")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.metadata.currentPage").value(2))
                .andExpect(jsonPath("$.metadata.totalPages").value(10))
                .andExpect(jsonPath("$.metadata.hasNext").value(true))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(true));
    }

    @Test
    void getExchangeRatesByDateRange_WhenSizeExceedsMaxSize_ShouldLimitToMaxSize() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        ExternalExchangeRateResponseDto rate = new ExternalExchangeRateResponseDto();
        rate.setId(1L);

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(1)
                .pageSize(500)
                .totalElements(100)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<ExternalExchangeRateResponseDto> pageResponse =
                PageResponse.<ExternalExchangeRateResponseDto>builder()
                        .content(List.of(rate))
                        .metadata(metadata)
                        .build();

        // Requesting 1000 but should be limited to 500 (maxSize)
        when(exchangeRateService.getExchangeRatesByDateRange(startDate, endDate, 0, 500))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.pageSize").value(500));
    }

    @Test
    void getLatestRatesByDate_ShouldReturnRates() throws Exception {
        // Given
        ExternalExchangeRateResponseDto rate1 = new ExternalExchangeRateResponseDto();
        rate1.setId(1L);
        rate1.setRate(BigDecimal.valueOf(0.92));

        ExternalExchangeRateResponseDto rate2 = new ExternalExchangeRateResponseDto();
        rate2.setId(2L);
        rate2.setRate(BigDecimal.valueOf(1.15));

        when(exchangeRateService.getLatestRatesByDateAndCurrencyFrom(
                LocalDate.of(2024, 1, 15), 2L))
                .thenReturn(List.of(rate1, rate2));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/latest/2024-01-15")
                        .param("currencyFromId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rate").value(0.92))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].rate").value(1.15));
    }

    @Test
    void calculateCrossRate_ShouldReturnCalculatedRate() throws Exception {
        // Given
        when(exchangeRateService.calculateCrossRate(1L, 2L, 3L, LocalDate.of(2024, 1, 15)))
                .thenReturn(BigDecimal.valueOf(1.08));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/cross-rate")
                        .param("currencyFromId", "1")
                        .param("currencyToId", "2")
                        .param("intermediateCurrencyId", "3")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1.08));
    }

    @Test
    void createExchangeRate_WhenValid_ShouldReturnCreated() throws Exception {
        // Given
        ExternalExchangeRateRequestDto requestDTO = new ExternalExchangeRateRequestDto();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(BigDecimal.valueOf(0.92));
        requestDTO.setSource("ECB");

        ExternalExchangeRateResponseDto responseDTO = new ExternalExchangeRateResponseDto();
        responseDTO.setId(1L);
        responseDTO.setRate(BigDecimal.valueOf(0.92));

        when(exchangeRateService.createExchangeRate(any(ExternalExchangeRateRequestDto.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rate").value(0.92));
    }

    @Test
    void createExchangeRate_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Given - missing required fields
        ExternalExchangeRateRequestDto requestDTO = new ExternalExchangeRateRequestDto();

        // When & Then
        mockMvc.perform(post("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExchangeRate_WhenValid_ShouldReturnUpdated() throws Exception {
        // Given
        ExternalExchangeRateRequestDto requestDTO = new ExternalExchangeRateRequestDto();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(BigDecimal.valueOf(0.93));
        requestDTO.setSource("ECB");

        ExternalExchangeRateResponseDto responseDTO = new ExternalExchangeRateResponseDto();
        responseDTO.setId(1L);
        responseDTO.setRate(BigDecimal.valueOf(0.93));

        when(exchangeRateService.updateExchangeRate(anyLong(), any(ExternalExchangeRateRequestDto.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/exchange-rates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(0.93));
    }

    @Test
    void activateExchangeRate_WhenExists_ShouldReturnActivated() throws Exception {
        // Given
        ExternalExchangeRateResponseDto responseDTO = new ExternalExchangeRateResponseDto();
        responseDTO.setId(1L);
        responseDTO.setIsActive(true);

        when(exchangeRateService.activateExchangeRate(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/exchange-rates/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void deactivateExchangeRate_WhenExists_ShouldReturnDeactivated() throws Exception {
        // Given
        ExternalExchangeRateResponseDto responseDTO = new ExternalExchangeRateResponseDto();
        responseDTO.setId(1L);
        responseDTO.setIsActive(false);

        when(exchangeRateService.deactivateExchangeRate(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/exchange-rates/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void deleteExchangeRate_WhenExists_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/exchange-rates/1"))
                .andExpect(status().isNoContent());
    }
}
package com.tarasantoniuk.finance.externalexchangerate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateRequestDTO;
import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateResponseDTO;
import com.tarasantoniuk.finance.externalexchangerate.service.ExternalExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
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
    void getAllExchangeRates_ShouldReturnListOfRates() throws Exception {
        // Given
        ExternalExchangeRateResponseDTO rate1 = new ExternalExchangeRateResponseDTO();
        rate1.setId(1L);
        rate1.setExchangeDate(LocalDate.now());
        rate1.setRate(BigDecimal.valueOf(0.92));

        ExternalExchangeRateResponseDTO rate2 = new ExternalExchangeRateResponseDTO();
        rate2.setId(2L);
        rate2.setExchangeDate(LocalDate.now());
        rate2.setRate(BigDecimal.valueOf(38.50));

        List<ExternalExchangeRateResponseDTO> rates = Arrays.asList(rate1, rate2);
        when(exchangeRateService.getAllExchangeRates()).thenReturn(rates);

        // When & Then
        mockMvc.perform(get("/api/exchange-rates"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].rate").value(0.92))
                .andExpect(jsonPath("$[1].rate").value(38.50));
    }

    @Test
    void getExchangeRateById_WhenExists_ShouldReturnRate() throws Exception {
        // Given
        ExternalExchangeRateResponseDTO rate = new ExternalExchangeRateResponseDTO();
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
        ExternalExchangeRateResponseDTO rate = new ExternalExchangeRateResponseDTO();
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
        ExternalExchangeRateResponseDTO rate = new ExternalExchangeRateResponseDTO();
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
    void getExchangeRatesByCurrencyPair_ShouldReturnFilteredList() throws Exception {
        // Given
        ExternalExchangeRateResponseDTO rate = new ExternalExchangeRateResponseDTO();
        rate.setId(1L);
        rate.setRate(BigDecimal.valueOf(0.92));

        when(exchangeRateService.getExchangeRatesByCurrencyPair(1L, 2L))
                .thenReturn(List.of(rate));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/currency-pair")
                        .param("currencyFromId", "1")
                        .param("currencyToId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getExchangeRatesBySource_ShouldReturnFilteredList() throws Exception {
        // Given
        ExternalExchangeRateResponseDTO rate = new ExternalExchangeRateResponseDTO();
        rate.setId(1L);

        when(exchangeRateService.getExchangeRatesBySource("NBU"))
                .thenReturn(List.of(rate));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/source/NBU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getExchangeRatesByDateRange_ShouldReturnFilteredList() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        ExternalExchangeRateResponseDTO rate = new ExternalExchangeRateResponseDTO();
        rate.setId(1L);

        when(exchangeRateService.getExchangeRatesByDateRange(startDate, endDate))
                .thenReturn(Arrays.asList(rate));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getLatestRatesByDate_ShouldReturnRates() throws Exception {
        // Given
        ExternalExchangeRateResponseDTO rate1 = new ExternalExchangeRateResponseDTO();
        rate1.setId(1L);
        rate1.setRate(BigDecimal.valueOf(0.92));

        ExternalExchangeRateResponseDTO rate2 = new ExternalExchangeRateResponseDTO();
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
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(BigDecimal.valueOf(0.92));
        requestDTO.setSource("ECB");

        ExternalExchangeRateResponseDTO responseDTO = new ExternalExchangeRateResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setRate(BigDecimal.valueOf(0.92));

        when(exchangeRateService.createExchangeRate(any(ExternalExchangeRateRequestDTO.class)))
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
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();

        // When & Then
        mockMvc.perform(post("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExchangeRate_WhenValid_ShouldReturnUpdated() throws Exception {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(BigDecimal.valueOf(0.93));
        requestDTO.setSource("ECB");

        ExternalExchangeRateResponseDTO responseDTO = new ExternalExchangeRateResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setRate(BigDecimal.valueOf(0.93));

        when(exchangeRateService.updateExchangeRate(anyLong(), any(ExternalExchangeRateRequestDTO.class)))
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
        ExternalExchangeRateResponseDTO responseDTO = new ExternalExchangeRateResponseDTO();
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
        ExternalExchangeRateResponseDTO responseDTO = new ExternalExchangeRateResponseDTO();
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
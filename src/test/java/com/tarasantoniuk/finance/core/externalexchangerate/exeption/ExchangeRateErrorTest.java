package com.tarasantoniuk.finance.core.externalexchangerate.exeption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.core.externalexchangerate.controller.ExternalExchangeRateController;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateRequestDTO;
import com.tarasantoniuk.finance.core.externalexchangerate.exception.ExchangeRateAlreadyExistsException;
import com.tarasantoniuk.finance.core.externalexchangerate.exception.ExchangeRateNotFoundException;
import com.tarasantoniuk.finance.core.externalexchangerate.exception.InvalidExchangeRateException;
import com.tarasantoniuk.finance.core.externalexchangerate.service.ExternalExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тести для помилкових сценаріїв ExchangeRate
 */
@WebMvcTest(ExternalExchangeRateController.class)
class ExchangeRateErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExternalExchangeRateService exchangeRateService;

    @Test
    void getExchangeRateById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(exchangeRateService.getExchangeRateById(999L))
                .thenThrow(ExchangeRateNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ExchangeRate not found with id: '999'"));
    }

    @Test
    void getLatestRatesByDate_WhenCurrencyNotFound_ShouldReturn404() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(exchangeRateService.getLatestRatesByDateAndCurrencyFrom(date, 999L))
                .thenThrow(CurrencyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/latest/2024-01-15")
                        .param("currencyFromId", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createExchangeRate_WhenCurrencyFromNotExists_ShouldReturn404() throws Exception {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(999L);
        requestDTO.setCurrencyToId(1L);
        requestDTO.setRate(BigDecimal.valueOf(1.0));
        requestDTO.setSource("TEST");

        when(exchangeRateService.createExchangeRate(any(ExternalExchangeRateRequestDTO.class)))
                .thenThrow(CurrencyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(post("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Currency not found with id: '999'"));
    }

    @Test
    void createExchangeRate_WhenCurrencyToNotExists_ShouldReturn404() throws Exception {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(999L);
        requestDTO.setRate(BigDecimal.valueOf(1.0));
        requestDTO.setSource("TEST");

        when(exchangeRateService.createExchangeRate(any(ExternalExchangeRateRequestDTO.class)))
                .thenThrow(CurrencyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(post("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createExchangeRate_WhenSameCurrency_ShouldReturn400() throws Exception {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(1L);
        requestDTO.setRate(BigDecimal.valueOf(1.0));
        requestDTO.setSource("TEST");

        when(exchangeRateService.createExchangeRate(any(ExternalExchangeRateRequestDTO.class)))
                .thenThrow(InvalidExchangeRateException.sameCurrency());

        // When & Then
        mockMvc.perform(post("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Currency From and Currency To cannot be the same"));
    }

    @Test
    void createExchangeRate_WhenAlreadyExists_ShouldReturn409() throws Exception {
        // Given
        LocalDate date = LocalDate.now();
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(date);
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(BigDecimal.valueOf(0.92));
        requestDTO.setSource("ECB");

        when(exchangeRateService.createExchangeRate(any(ExternalExchangeRateRequestDTO.class)))
                .thenThrow(ExchangeRateAlreadyExistsException.forDateCurrencyAndSource(date, 1L, 2L, "ECB"));

        // When & Then
        mockMvc.perform(post("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createExchangeRate_WhenInvalidRate_ShouldReturn400() throws Exception {
        // Given - rate must be positive
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(BigDecimal.valueOf(-1.0));
        requestDTO.setSource("TEST");

        // When & Then
        mockMvc.perform(post("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createExchangeRate_WhenMissingRequiredFields_ShouldReturn400() throws Exception {
        // Given - missing required fields
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        // Всі поля пусті

        // When & Then
        mockMvc.perform(post("/api/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void calculateCrossRate_WhenFirstRateMissing_ShouldReturn404() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(exchangeRateService.calculateCrossRate(1L, 2L, 3L, date))
                .thenThrow(ExchangeRateNotFoundException.byDateAndCurrencyPair(date, 1L, 3L));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/cross-rate")
                        .param("currencyFromId", "1")
                        .param("currencyToId", "2")
                        .param("intermediateCurrencyId", "3")
                        .param("date", "2024-01-15"))
                .andExpect(status().isNotFound());
    }

    @Test
    void calculateCrossRate_WhenSecondRateMissing_ShouldReturn404() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(exchangeRateService.calculateCrossRate(1L, 2L, 3L, date))
                .thenThrow(ExchangeRateNotFoundException.byDateAndCurrencyPair(date, 3L, 2L));

        // When & Then
        mockMvc.perform(get("/api/exchange-rates/cross-rate")
                        .param("currencyFromId", "1")
                        .param("currencyToId", "2")
                        .param("intermediateCurrencyId", "3")
                        .param("date", "2024-01-15"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateExchangeRate_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        ExternalExchangeRateRequestDTO requestDTO = new ExternalExchangeRateRequestDTO();
        requestDTO.setExchangeDate(LocalDate.now());
        requestDTO.setCurrencyFromId(1L);
        requestDTO.setCurrencyToId(2L);
        requestDTO.setRate(BigDecimal.valueOf(0.92));
        requestDTO.setSource("TEST");

        when(exchangeRateService.updateExchangeRate(anyLong(), any(ExternalExchangeRateRequestDTO.class)))
                .thenThrow(ExchangeRateNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(put("/api/exchange-rates/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateExchangeRate_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(exchangeRateService.activateExchangeRate(999L))
                .thenThrow(ExchangeRateNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(patch("/api/exchange-rates/999/activate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateExchangeRate_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(exchangeRateService.deactivateExchangeRate(999L))
                .thenThrow(ExchangeRateNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(patch("/api/exchange-rates/999/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExchangeRate_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        doThrow(ExchangeRateNotFoundException.byId(999L))
                .when(exchangeRateService).deleteExchangeRate(999L);

        // When & Then
        mockMvc.perform(delete("/api/exchange-rates/999"))
                .andExpect(status().isNotFound());
    }
}
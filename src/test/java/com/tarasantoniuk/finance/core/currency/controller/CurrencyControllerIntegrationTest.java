package com.tarasantoniuk.finance.core.currency.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.core.currency.controller.CurrencyController;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDTO;
import com.tarasantoniuk.finance.core.currency.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrencyService currencyService;

    @Test
    void getAllCurrencies_ShouldReturnListOfCurrencies() throws Exception {
        // Given
        CurrencyResponseDTO usd = new CurrencyResponseDTO();
        usd.setId(1L);
        usd.setCode("USD");
        usd.setName("US Dollar");

        CurrencyResponseDTO eur = new CurrencyResponseDTO();
        eur.setId(2L);
        eur.setCode("EUR");
        eur.setName("Euro");

        List<CurrencyResponseDTO> currencies = Arrays.asList(usd, eur);
        when(currencyService.getAllCurrencies()).thenReturn(currencies);

        // When & Then
        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].code").value("USD"))
                .andExpect(jsonPath("$[1].code").value("EUR"));
    }

    @Test
    void getActiveCurrencies_ShouldReturnOnlyActiveCurrencies() throws Exception {
        // Given
        CurrencyResponseDTO usd = new CurrencyResponseDTO();
        usd.setId(1L);
        usd.setCode("USD");
        usd.setIsActive(true);

        when(currencyService.getActiveCurrencies()).thenReturn(Arrays.asList(usd));

        // When & Then
        mockMvc.perform(get("/api/currencies/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].code").value("USD"));
    }

    @Test
    void getCurrencyById_WhenExists_ShouldReturnCurrency() throws Exception {
        // Given
        CurrencyResponseDTO currency = new CurrencyResponseDTO();
        currency.setId(1L);
        currency.setCode("USD");
        currency.setNumericCode("840");

        when(currencyService.getCurrencyById(1L)).thenReturn(currency);

        // When & Then
        mockMvc.perform(get("/api/currencies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("USD"));
    }

    @Test
    void getCurrencyByCode_WhenExists_ShouldReturnCurrency() throws Exception {
        // Given
        CurrencyResponseDTO currency = new CurrencyResponseDTO();
        currency.setId(1L);
        currency.setCode("USD");

        when(currencyService.getCurrencyByCode("USD")).thenReturn(currency);

        // When & Then
        mockMvc.perform(get("/api/currencies/code/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("USD"));
    }

    @Test
    void getCurrencyByNumericCode_WhenExists_ShouldReturnCurrency() throws Exception {
        // Given
        CurrencyResponseDTO currency = new CurrencyResponseDTO();
        currency.setId(1L);
        currency.setCode("USD");
        currency.setNumericCode("840");

        when(currencyService.getCurrencyByNumericCode("840")).thenReturn(currency);

        // When & Then
        mockMvc.perform(get("/api/currencies/numeric/840"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numericCode").value("840"));
    }

    @Test
    void searchCurrenciesByName_ShouldReturnMatchingCurrencies() throws Exception {
        // Given
        CurrencyResponseDTO currency = new CurrencyResponseDTO();
        currency.setId(1L);
        currency.setCode("USD");
        currency.setName("US Dollar");

        when(currencyService.searchCurrenciesByName("Dollar"))
                .thenReturn(Arrays.asList(currency));

        // When & Then
        mockMvc.perform(get("/api/currencies/search")
                        .param("name", "Dollar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("US Dollar"));
    }

    @Test
    void createCurrency_WhenValid_ShouldReturnCreated() throws Exception {
        // Given
        CurrencyRequestDTO requestDTO = new CurrencyRequestDTO();
        requestDTO.setCode("UAH");
        requestDTO.setNumericCode("980");
        requestDTO.setName("Ukrainian Hryvnia");
        requestDTO.setSymbol("₴");
        requestDTO.setMinorUnit(2);

        CurrencyResponseDTO responseDTO = new CurrencyResponseDTO();
        responseDTO.setId(3L);
        responseDTO.setCode("UAH");
        responseDTO.setNumericCode("980");

        when(currencyService.createCurrency(any(CurrencyRequestDTO.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.code").value("UAH"));
    }

    @Test
    void createCurrency_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Given - missing required fields
        CurrencyRequestDTO requestDTO = new CurrencyRequestDTO();

        // When & Then
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrency_WhenValid_ShouldReturnUpdated() throws Exception {
        // Given
        CurrencyRequestDTO requestDTO = new CurrencyRequestDTO();
        requestDTO.setCode("USD");
        requestDTO.setNumericCode("840");
        requestDTO.setName("United States Dollar");
        requestDTO.setSymbol("$");
        requestDTO.setMinorUnit(2);

        CurrencyResponseDTO responseDTO = new CurrencyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("United States Dollar");

        when(currencyService.updateCurrency(anyLong(), any(CurrencyRequestDTO.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/currencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("United States Dollar"));
    }

    @Test
    void activateCurrency_WhenExists_ShouldReturnActivated() throws Exception {
        // Given
        CurrencyResponseDTO responseDTO = new CurrencyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setCode("USD");
        responseDTO.setIsActive(true);

        when(currencyService.activateCurrency(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/currencies/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void deactivateCurrency_WhenExists_ShouldReturnDeactivated() throws Exception {
        // Given
        CurrencyResponseDTO responseDTO = new CurrencyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setCode("USD");
        responseDTO.setIsActive(false);

        when(currencyService.deactivateCurrency(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/currencies/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void deleteCurrency_WhenExists_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/currencies/1"))
                .andExpect(status().isNoContent());
    }
}
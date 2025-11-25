package com.tarasantoniuk.finance.core.currency.exeption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.core.currency.controller.CurrencyController;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyRequestDto;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyAlreadyExistsException;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.core.currency.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тести для помилкових сценаріїв Currency
 */
@WebMvcTest(CurrencyController.class)
class CurrencyErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrencyService currencyService;

    @Test
    void getCurrencyById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(currencyService.getCurrencyById(999L))
                .thenThrow(CurrencyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(get("/api/currencies/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrencyByCode_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(currencyService.getCurrencyByCode("XXX"))
                .thenThrow(CurrencyNotFoundException.byCode("XXX"));

        // When & Then
        mockMvc.perform(get("/api/currencies/code/XXX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Currency not found with code: 'XXX'"));
    }

    @Test
    void getCurrencyByNumericCode_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(currencyService.getCurrencyByNumericCode("999"))
                .thenThrow(CurrencyNotFoundException.byNumericCode("999"));

        // When & Then
        mockMvc.perform(get("/api/currencies/numeric/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCurrency_WhenCodeExists_ShouldReturn409() throws Exception {
        // Given
        CurrencyRequestDto requestDTO = new CurrencyRequestDto();
        requestDTO.setCode("USD");
        requestDTO.setNumericCode("840");
        requestDTO.setName("US Dollar");
        requestDTO.setMinorUnit(2);

        when(currencyService.createCurrency(any(CurrencyRequestDto.class)))
                .thenThrow(CurrencyAlreadyExistsException.byCode("USD"));

        // When & Then
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Currency already exists with code: 'USD'"));
    }

    @Test
    void createCurrency_WhenNumericCodeExists_ShouldReturn409() throws Exception {
        // Given
        CurrencyRequestDto requestDTO = new CurrencyRequestDto();
        requestDTO.setCode("XXX");
        requestDTO.setNumericCode("840");
        requestDTO.setName("Test Currency");
        requestDTO.setMinorUnit(2);

        when(currencyService.createCurrency(any(CurrencyRequestDto.class)))
                .thenThrow(CurrencyAlreadyExistsException.byNumericCode("840"));

        // When & Then
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCurrency_WhenInvalidCodeFormat_ShouldReturn400() throws Exception {
        // Given - code must be 3 uppercase letters
        CurrencyRequestDto requestDTO = new CurrencyRequestDto();
        requestDTO.setCode("us");
        requestDTO.setNumericCode("840");
        requestDTO.setName("US Dollar");
        requestDTO.setMinorUnit(2);

        // When & Then
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void createCurrency_WhenInvalidNumericCodeFormat_ShouldReturn400() throws Exception {
        // Given - numeric code must be 3 digits
        CurrencyRequestDto requestDTO = new CurrencyRequestDto();
        requestDTO.setCode("USD");
        requestDTO.setNumericCode("84");
        requestDTO.setName("US Dollar");
        requestDTO.setMinorUnit(2);

        // When & Then
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCurrency_WhenMissingRequiredFields_ShouldReturn400() throws Exception {
        // Given - missing required fields
        CurrencyRequestDto requestDTO = new CurrencyRequestDto();

        // When & Then
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void updateCurrency_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        CurrencyRequestDto requestDTO = new CurrencyRequestDto();
        requestDTO.setCode("USD");
        requestDTO.setNumericCode("840");
        requestDTO.setName("US Dollar");
        requestDTO.setMinorUnit(2);

        when(currencyService.updateCurrency(any(), any(CurrencyRequestDto.class)))
                .thenThrow(CurrencyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(put("/api/currencies/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateCurrency_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(currencyService.activateCurrency(999L))
                .thenThrow(CurrencyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(patch("/api/currencies/999/activate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateCurrency_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(currencyService.deactivateCurrency(999L))
                .thenThrow(CurrencyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(patch("/api/currencies/999/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCurrency_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        doThrow(CurrencyNotFoundException.byId(999L))
                .when(currencyService).deleteCurrency(999L);

        // When & Then
        mockMvc.perform(delete("/api/currencies/999"))
                .andExpect(status().isNotFound());
    }
}
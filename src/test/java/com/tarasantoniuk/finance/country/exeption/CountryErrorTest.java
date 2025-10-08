package com.tarasantoniuk.finance.country.exeption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.country.controller.CountryController;
import com.tarasantoniuk.finance.country.dto.CountryRequestDTO;
import com.tarasantoniuk.finance.country.exception.CountryAlreadyExistsException;
import com.tarasantoniuk.finance.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.country.service.CountryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тести для помилкових сценаріїв Country
 */
@WebMvcTest(CountryController.class)
class CountryErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CountryService countryService;

    @Test
    void getCountryById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(countryService.getCountryById(999L))
                .thenThrow(CountryNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(get("/api/countries/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getCountryByIsoCode_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(countryService.getCountryByIsoCode("XXX"))
                .thenThrow(CountryNotFoundException.byIsoCode("XXX"));

        // When & Then
        mockMvc.perform(get("/api/countries/iso/XXX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCountry_WhenIsoCodeExists_ShouldReturn409() throws Exception {
        // Given
        CountryRequestDTO requestDTO = new CountryRequestDTO();
        requestDTO.setName("United States");
        requestDTO.setIsoCode("USA");

        when(countryService.createCountry(any(CountryRequestDTO.class)))
                .thenThrow(CountryAlreadyExistsException.byIsoCode("USA"));

        // When & Then
        mockMvc.perform(post("/api/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void createCountry_WhenInvalidIsoCode_ShouldReturn400() throws Exception {
        // Given - ISO code must be 2-3 characters
        CountryRequestDTO requestDTO = new CountryRequestDTO();
        requestDTO.setName("Test Country");
        requestDTO.setIsoCode("TOOLONG");

        // When & Then
        mockMvc.perform(post("/api/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void createCountry_WhenMissingRequiredFields_ShouldReturn400() throws Exception {
        // Given - missing name and isoCode
        CountryRequestDTO requestDTO = new CountryRequestDTO();

        // When & Then
        mockMvc.perform(post("/api/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void updateCountry_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        CountryRequestDTO requestDTO = new CountryRequestDTO();
        requestDTO.setName("Test");
        requestDTO.setIsoCode("TST");

        when(countryService.updateCountry(anyLong(), any(CountryRequestDTO.class)))
                .thenThrow(CountryNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(put("/api/countries/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCountry_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(countryService.getCountryById(999L))
                .thenThrow(CountryNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(delete("/api/countries/999"))
                .andExpect(status().isNotFound());
    }
}
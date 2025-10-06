package com.tarasantoniuk.finance.country.controller;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.country.dto.CountryRequestDTO;
import com.tarasantoniuk.finance.country.dto.CountryResponseDTO;
import com.tarasantoniuk.finance.country.service.CountryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test для CountryController
 * Використовує MockMvc для тестування REST endpoints
 */
@WebMvcTest(CountryController.class)
class CountryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CountryService countryService;

    @Test
    void getAllCountries_ShouldReturnListOfCountries() throws Exception {
        // Given
        CountryResponseDTO country1 = new CountryResponseDTO();
        country1.setId(1L);
        country1.setName("United States");
        country1.setIsoCode("USA");

        CountryResponseDTO country2 = new CountryResponseDTO();
        country2.setId(2L);
        country2.setName("United Kingdom");
        country2.setIsoCode("GBR");

        List<CountryResponseDTO> countries = Arrays.asList(country1, country2);
        when(countryService.getAllCountries()).thenReturn(countries);

        // When & Then
        mockMvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].isoCode").value("USA"))
                .andExpect(jsonPath("$[1].isoCode").value("GBR"));
    }

    @Test
    void getCountryById_WhenExists_ShouldReturnCountry() throws Exception {
        // Given
        CountryResponseDTO country = new CountryResponseDTO();
        country.setId(1L);
        country.setName("United States");
        country.setIsoCode("USA");

        when(countryService.getCountryById(1L)).thenReturn(country);

        // When & Then
        mockMvc.perform(get("/api/countries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isoCode").value("USA"));
    }

    @Test
    void createCountry_WhenValid_ShouldReturnCreated() throws Exception {
        // Given
        CountryRequestDTO requestDTO = new CountryRequestDTO();
        requestDTO.setName("France");
        requestDTO.setIsoCode("FRA");
        requestDTO.setPhoneCode("+33");

        CountryResponseDTO responseDTO = new CountryResponseDTO();
        responseDTO.setId(3L);
        responseDTO.setName("France");
        responseDTO.setIsoCode("FRA");

        when(countryService.createCountry(any(CountryRequestDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.isoCode").value("FRA"));
    }

    @Test
    void createCountry_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Given - invalid request (missing required fields)
        CountryRequestDTO requestDTO = new CountryRequestDTO();
        // name and isoCode are missing (validation will fail)

        // When & Then
        mockMvc.perform(post("/api/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCountry_WhenExists_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/countries/1"))
                .andExpect(status().isNoContent());
    }
}
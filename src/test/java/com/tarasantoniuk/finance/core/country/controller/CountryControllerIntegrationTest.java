package com.tarasantoniuk.finance.core.country.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.core.country.dto.CountryRequestDto;
import com.tarasantoniuk.finance.core.country.dto.CountryResponseDto;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.service.CountryService;
import com.tarasantoniuk.finance.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
 * Integration test for CountryController
 * Uses MockMvc to test REST endpoints
 */
@WebMvcTest(CountryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CountryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CountryService countryService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getAllCountries_ShouldReturnListOfCountries() throws Exception {
        // Given
        CountryResponseDto country1 = new CountryResponseDto();
        country1.setId(1L);
        country1.setName("United States");
        country1.setIsoCode("USA");

        CountryResponseDto country2 = new CountryResponseDto();
        country2.setId(2L);
        country2.setName("United Kingdom");
        country2.setIsoCode("GBR");

        List<CountryResponseDto> countries = Arrays.asList(country1, country2);
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
        CountryResponseDto country = new CountryResponseDto();
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
    void getCountryByIsoCode_WhenExists_ShouldReturnCountry() throws Exception {
        // Given
        CountryResponseDto country = new CountryResponseDto();
        country.setId(1L);
        country.setName("United States");
        country.setIsoCode("USA");

        when(countryService.getCountryByIsoCode("USA")).thenReturn(country);

        // When & Then
        mockMvc.perform(get("/api/countries/iso/USA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isoCode").value("USA"))
                .andExpect(jsonPath("$.name").value("United States"));
    }

    @Test
    void getCountryByIsoCode_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(countryService.getCountryByIsoCode("XXX"))
                .thenThrow(CountryNotFoundException.byIsoCode("XXX"));

        // When & Then
        mockMvc.perform(get("/api/countries/iso/XXX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCountry_WhenValid_ShouldReturnCreated() throws Exception {
        // Given
        CountryRequestDto requestDTO = new CountryRequestDto();
        requestDTO.setName("France");
        requestDTO.setIsoCode("FRA");
        requestDTO.setPhoneCode("+33");

        CountryResponseDto responseDTO = new CountryResponseDto();
        responseDTO.setId(3L);
        responseDTO.setName("France");
        responseDTO.setIsoCode("FRA");

        when(countryService.createCountry(any(CountryRequestDto.class))).thenReturn(responseDTO);

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
        CountryRequestDto requestDTO = new CountryRequestDto();
        // name and isoCode are missing (validation will fail)

        // When & Then
        mockMvc.perform(post("/api/countries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCountry_WhenValid_ShouldReturnUpdatedCountry() throws Exception {
        // Given
        CountryRequestDto requestDTO = new CountryRequestDto();
        requestDTO.setName("United States of America");
        requestDTO.setIsoCode("USA");
        requestDTO.setPhoneCode("+1");

        CountryResponseDto responseDTO = new CountryResponseDto();
        responseDTO.setId(1L);
        responseDTO.setName("United States of America");
        responseDTO.setIsoCode("USA");

        when(countryService.updateCountry(any(Long.class), any(CountryRequestDto.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/countries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("United States of America"))
                .andExpect(jsonPath("$.isoCode").value("USA"));
    }

    @Test
    void updateCountry_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        CountryRequestDto requestDTO = new CountryRequestDto();
        requestDTO.setName("United States");
        requestDTO.setIsoCode("USA");
        requestDTO.setPhoneCode("+1");

        when(countryService.updateCountry(any(Long.class), any(CountryRequestDto.class)))
                .thenThrow(CountryNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(put("/api/countries/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCountry_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Given - invalid request (missing required fields)
        CountryRequestDto requestDTO = new CountryRequestDto();

        // When & Then
        mockMvc.perform(put("/api/countries/1")
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
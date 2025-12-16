package com.tarasantoniuk.finance.core.organization.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.organization.controller.OrganizationController;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationRequestDto;
import com.tarasantoniuk.finance.core.organization.service.OrganizationService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тести для помилкових сценаріїв Organization
 */
@WebMvcTest(OrganizationController.class)
class OrganizationErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @Test
    void getOrganizationById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(organizationService.getOrganizationById(999L))
                .thenThrow(OrganizationNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(get("/api/organizations/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Organization not found with id: '999'"));
    }

    @Test
    void createOrganization_WhenCountryNotExists_ShouldReturn404() throws Exception {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Test Org");
        requestDTO.setCountryId(999L);

        when(organizationService.createOrganization(any(OrganizationRequestDto.class)))
                .thenThrow(CountryNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Country not found with id: '999'"));
    }

    @Test
    void createOrganization_WhenRegistrationNumberExists_ShouldReturn409() throws Exception {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Test Org");
        requestDTO.setRegistrationNumber("12345678");
        requestDTO.setCountryId(1L);

        when(organizationService.createOrganization(any(OrganizationRequestDto.class)))
                .thenThrow(OrganizationAlreadyExistsException.byRegistrationNumber("12345678"));

        // When & Then
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void createOrganization_WhenInvalidEmail_ShouldReturn400() throws Exception {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Test Org");
        requestDTO.setEmail("invalid-email");
        requestDTO.setCountryId(1L);

        // When & Then
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void createOrganization_WhenMissingRequiredFields_ShouldReturn400() throws Exception {
        // Given - missing name and countryId
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();

        // When & Then
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void updateOrganization_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Updated Org");
        requestDTO.setCountryId(1L);

        when(organizationService.updateOrganization(any(), any(OrganizationRequestDto.class)))
                .thenThrow(OrganizationNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(put("/api/organizations/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrganization_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        doThrow(OrganizationNotFoundException.byId(999L))
                .when(organizationService).deleteOrganization(999L);

        // When & Then
        mockMvc.perform(delete("/api/organizations/999"))
                .andExpect(status().isNotFound());
    }
}
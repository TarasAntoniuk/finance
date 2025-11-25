package com.tarasantoniuk.finance.core.organization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationRequestDto;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDto;
import com.tarasantoniuk.finance.core.organization.service.OrganizationService;
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

@WebMvcTest(OrganizationController.class)
class OrganizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @Test
    void getAllOrganizations_ShouldReturnListOfOrganizations() throws Exception {
        // Given
        OrganizationResponseDto org1 = new OrganizationResponseDto();
        org1.setId(1L);
        org1.setName("Acme Corp");
        org1.setRegistrationNumber("12345678");

        OrganizationResponseDto org2 = new OrganizationResponseDto();
        org2.setId(2L);
        org2.setName("Tech Solutions");
        org2.setRegistrationNumber("87654321");

        List<OrganizationResponseDto> organizations = Arrays.asList(org1, org2);
        when(organizationService.getAllOrganizations()).thenReturn(organizations);

        // When & Then
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Acme Corp"))
                .andExpect(jsonPath("$[1].name").value("Tech Solutions"));
    }

    @Test
    void getOrganizationById_WhenExists_ShouldReturnOrganization() throws Exception {
        // Given
        OrganizationResponseDto organization = new OrganizationResponseDto();
        organization.setId(1L);
        organization.setName("Acme Corp");
        organization.setRegistrationNumber("12345678");

        when(organizationService.getOrganizationById(1L)).thenReturn(organization);

        // When & Then
        mockMvc.perform(get("/api/organizations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Acme Corp"));
    }

    @Test
    void getOrganizationsByCountry_ShouldReturnFilteredList() throws Exception {
        // Given
        OrganizationResponseDto organization = new OrganizationResponseDto();
        organization.setId(1L);
        organization.setName("Ukrainian Company");

        when(organizationService.getOrganizationsByCountry(1L))
                .thenReturn(List.of(organization));

        // When & Then
        mockMvc.perform(get("/api/organizations/country/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Ukrainian Company"));
    }

    @Test
    void searchOrganizationsByName_ShouldReturnMatchingOrganizations() throws Exception {
        // Given
        OrganizationResponseDto organization = new OrganizationResponseDto();
        organization.setId(1L);
        organization.setName("Acme Corp");

        when(organizationService.searchOrganizationsByName("Acme"))
                .thenReturn(List.of(organization));

        // When & Then
        mockMvc.perform(get("/api/organizations/search")
                        .param("name", "Acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Acme Corp"));
    }

    @Test
    void createOrganization_WhenValid_ShouldReturnCreated() throws Exception {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("New Company");
        requestDTO.setRegistrationNumber("99999999");
        requestDTO.setCountryId(1L);
        requestDTO.setEmail("info@newcompany.com");

        OrganizationResponseDto responseDTO = new OrganizationResponseDto();
        responseDTO.setId(3L);
        responseDTO.setName("New Company");
        responseDTO.setRegistrationNumber("99999999");

        when(organizationService.createOrganization(any(OrganizationRequestDto.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Company"));
    }

    @Test
    void createOrganization_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Given - missing required fields
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();

        // When & Then
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateOrganization_WhenValid_ShouldReturnUpdated() throws Exception {
        // Given
        OrganizationRequestDto requestDTO = new OrganizationRequestDto();
        requestDTO.setName("Updated Company");
        requestDTO.setRegistrationNumber("12345678");
        requestDTO.setCountryId(1L);

        OrganizationResponseDto responseDTO = new OrganizationResponseDto();
        responseDTO.setId(1L);
        responseDTO.setName("Updated Company");

        when(organizationService.updateOrganization(anyLong(), any(OrganizationRequestDto.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/organizations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Company"));
    }

    @Test
    void deleteOrganization_WhenExists_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/organizations/1"))
                .andExpect(status().isNoContent());
    }
}
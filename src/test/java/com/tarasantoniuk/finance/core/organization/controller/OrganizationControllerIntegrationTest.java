package com.tarasantoniuk.finance.core.organization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationRequestDto;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDto;
import com.tarasantoniuk.finance.core.organization.service.OrganizationService;
import com.tarasantoniuk.finance.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrganizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
        PageResponse<OrganizationResponseDto> pageResponse = PageResponse.<OrganizationResponseDto>builder()
                .content(organizations)
                .metadata(PageMetadata.builder()
                        .currentPage(0).totalPages(1).pageSize(50).totalElements(2).hasNext(false).hasPrevious(false).build())
                .build();
        when(organizationService.getAllOrganizations(anyInt(), anyInt())).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Acme Corp"))
                .andExpect(jsonPath("$.content[1].name").value("Tech Solutions"));
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

        when(organizationService.getOrganizationsByCountry(eq(1L), anyInt()))
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

        PageResponse<OrganizationResponseDto> pageResponse = PageResponse.<OrganizationResponseDto>builder()
                .content(List.of(organization))
                .metadata(PageMetadata.builder()
                        .currentPage(0).totalPages(1).pageSize(50).totalElements(1).hasNext(false).hasPrevious(false).build())
                .build();
        when(organizationService.searchOrganizationsByName(eq("Acme"), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/organizations/search")
                        .param("name", "Acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Acme Corp"));
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

    // ========== PAGINATION VALIDATION TESTS ==========

    @Test
    void getAllOrganizations_WhenSizeIsZero_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/organizations")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllOrganizations_WhenSizeIsNegative_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/organizations")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllOrganizations_WhenPageIsNegative_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/organizations")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrganizationsByCountry_WhenLimitIsZero_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/organizations/country/1")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrganizationsByCountry_WhenLimitIsNegative_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/organizations/country/1")
                        .param("limit", "-1"))
                .andExpect(status().isBadRequest());
    }

    // Page size clamping: anything above MAX_PAGE_SIZE (500) is capped

    @Test
    void getAllOrganizations_WhenSizeExceedsMaximum_ShouldClampToMaximum() throws Exception {
        when(organizationService.getAllOrganizations(anyInt(), anyInt()))
                .thenReturn(PageResponse.<OrganizationResponseDto>builder()
                        .content(List.of())
                        .metadata(PageMetadata.builder()
                                .currentPage(0).totalPages(0).pageSize(500)
                                .totalElements(0).hasNext(false).hasPrevious(false).build())
                        .build());

        mockMvc.perform(get("/api/organizations").param("size", "1000"))
                .andExpect(status().isOk());

        verify(organizationService).getAllOrganizations(0, 500);
    }

    @Test
    void getOrganizationsByCountry_WhenLimitExceedsMaximum_ShouldClampToMaximum() throws Exception {
        when(organizationService.getOrganizationsByCountry(anyLong(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/organizations/country/1").param("limit", "1000"))
                .andExpect(status().isOk());

        verify(organizationService).getOrganizationsByCountry(1L, 500);
    }

    @Test
    void searchOrganizationsByName_WhenSizeExceedsMaximum_ShouldClampToMaximum() throws Exception {
        when(organizationService.searchOrganizationsByName(anyString(), anyInt(), anyInt()))
                .thenReturn(PageResponse.<OrganizationResponseDto>builder()
                        .content(List.of())
                        .metadata(PageMetadata.builder()
                                .currentPage(0).totalPages(0).pageSize(500)
                                .totalElements(0).hasNext(false).hasPrevious(false).build())
                        .build());

        mockMvc.perform(get("/api/organizations/search").param("name", "Acme").param("size", "1000"))
                .andExpect(status().isOk());

        verify(organizationService).searchOrganizationsByName("Acme", 0, 500);
    }
}

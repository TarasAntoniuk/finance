package com.tarasantoniuk.finance.core.accountingpolicy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.core.accountingpolicy.controller.AccountingPolicyController;
import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyRequestDTO;
import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyResponseDTO;
import com.tarasantoniuk.finance.core.accountingpolicy.service.AccountingPolicyService;
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

@WebMvcTest(AccountingPolicyController.class)
class AccountingPolicyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountingPolicyService accountingPolicyService;

    @Test
    void getAllAccountingPolicies_ShouldReturnListOfPolicies() throws Exception {
        // Given
        AccountingPolicyResponseDTO policy1 = new AccountingPolicyResponseDTO();
        policy1.setId(1L);
        policy1.setYear(2024);

        AccountingPolicyResponseDTO policy2 = new AccountingPolicyResponseDTO();
        policy2.setId(2L);
        policy2.setYear(2023);

        List<AccountingPolicyResponseDTO> policies = Arrays.asList(policy1, policy2);
        when(accountingPolicyService.getAllAccountingPolicies()).thenReturn(policies);

        // When & Then
        mockMvc.perform(get("/api/accounting-policies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].year").value(2024))
                .andExpect(jsonPath("$[1].year").value(2023));
    }

    @Test
    void getAccountingPolicyById_WhenExists_ShouldReturnPolicy() throws Exception {
        // Given
        AccountingPolicyResponseDTO policy = new AccountingPolicyResponseDTO();
        policy.setId(1L);
        policy.setYear(2024);

        when(accountingPolicyService.getAccountingPolicyById(1L)).thenReturn(policy);

        // When & Then
        mockMvc.perform(get("/api/accounting-policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.year").value(2024));
    }

    @Test
    void getAccountingPolicyByOrganizationAndYear_WhenExists_ShouldReturnPolicy() throws Exception {
        // Given
        AccountingPolicyResponseDTO policy = new AccountingPolicyResponseDTO();
        policy.setId(1L);
        policy.setYear(2024);

        when(accountingPolicyService.getAccountingPolicyByOrganizationAndYear(1L, 2024))
                .thenReturn(policy);

        // When & Then
        mockMvc.perform(get("/api/accounting-policies/organization/1/year/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024));
    }

    @Test
    void getAccountingPoliciesByOrganization_ShouldReturnFilteredList() throws Exception {
        // Given
        AccountingPolicyResponseDTO policy = new AccountingPolicyResponseDTO();
        policy.setId(1L);
        policy.setYear(2024);

        when(accountingPolicyService.getAccountingPoliciesByOrganization(1L))
                .thenReturn(List.of(policy));

        // When & Then
        mockMvc.perform(get("/api/accounting-policies/organization/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].year").value(2024));
    }

    @Test
    void getActiveAccountingPoliciesByOrganization_ShouldReturnActivePolicies() throws Exception {
        // Given
        AccountingPolicyResponseDTO policy = new AccountingPolicyResponseDTO();
        policy.setId(1L);
        policy.setYear(2024);
        policy.setIsActive(true);

        when(accountingPolicyService.getActiveAccountingPoliciesByOrganization(1L))
                .thenReturn(List.of(policy));

        // When & Then
        mockMvc.perform(get("/api/accounting-policies/organization/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void getAccountingPoliciesByYear_ShouldReturnFilteredList() throws Exception {
        // Given
        AccountingPolicyResponseDTO policy = new AccountingPolicyResponseDTO();
        policy.setId(1L);
        policy.setYear(2024);

        when(accountingPolicyService.getAccountingPoliciesByYear(2024))
                .thenReturn(List.of(policy));

        // When & Then
        mockMvc.perform(get("/api/accounting-policies/year/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].year").value(2024));
    }

    @Test
    void getAccountingPoliciesByYearRange_ShouldReturnFilteredList() throws Exception {
        // Given
        AccountingPolicyResponseDTO policy = new AccountingPolicyResponseDTO();
        policy.setId(1L);
        policy.setYear(2024);

        when(accountingPolicyService.getAccountingPoliciesByYearRange(2020, 2024))
                .thenReturn(List.of(policy));

        // When & Then
        mockMvc.perform(get("/api/accounting-policies/year-range")
                        .param("startYear", "2020")
                        .param("endYear", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAccountingPoliciesByCurrency_ShouldReturnFilteredList() throws Exception {
        // Given
        AccountingPolicyResponseDTO policy = new AccountingPolicyResponseDTO();
        policy.setId(1L);
        policy.setYear(2024);

        when(accountingPolicyService.getAccountingPoliciesByCurrency(1L))
                .thenReturn(List.of(policy));

        // When & Then
        mockMvc.perform(get("/api/accounting-policies/currency/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createAccountingPolicy_WhenValid_ShouldReturnCreated() throws Exception {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(1L);
        requestDTO.setFiscalYearStartMonth(1);

        AccountingPolicyResponseDTO responseDTO = new AccountingPolicyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setYear(2024);

        when(accountingPolicyService.createAccountingPolicy(any(AccountingPolicyRequestDTO.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/accounting-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.year").value(2024));
    }

    @Test
    void createAccountingPolicy_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Given - missing required fields
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();

        // When & Then
        mockMvc.perform(post("/api/accounting-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAccountingPolicy_WhenValid_ShouldReturnUpdated() throws Exception {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(1L);

        AccountingPolicyResponseDTO responseDTO = new AccountingPolicyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setYear(2024);

        when(accountingPolicyService.updateAccountingPolicy(anyLong(), any(AccountingPolicyRequestDTO.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/accounting-policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024));
    }

    @Test
    void activateAccountingPolicy_WhenExists_ShouldReturnActivated() throws Exception {
        // Given
        AccountingPolicyResponseDTO responseDTO = new AccountingPolicyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setIsActive(true);

        when(accountingPolicyService.activateAccountingPolicy(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/accounting-policies/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void deactivateAccountingPolicy_WhenExists_ShouldReturnDeactivated() throws Exception {
        // Given
        AccountingPolicyResponseDTO responseDTO = new AccountingPolicyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setIsActive(false);

        when(accountingPolicyService.deactivateAccountingPolicy(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/accounting-policies/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void deleteAccountingPolicy_WhenExists_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/accounting-policies/1"))
                .andExpect(status().isNoContent());
    }
}
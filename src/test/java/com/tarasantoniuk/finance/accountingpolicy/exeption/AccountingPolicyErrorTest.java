package com.tarasantoniuk.finance.accountingpolicy.exeption;

import com.tarasantoniuk.finance.accountingpolicy.controller.AccountingPolicyController;
import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyRequestDTO;

import com.tarasantoniuk.finance.accountingpolicy.exception.AccountingPolicyAlreadyExistsException;
import com.tarasantoniuk.finance.accountingpolicy.exception.AccountingPolicyNotFoundException;
import com.tarasantoniuk.finance.accountingpolicy.service.AccountingPolicyService;
import com.tarasantoniuk.finance.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.organization.exeption.OrganizationNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Тести для помилкових сценаріїв AccountingPolicy
 */
@WebMvcTest(AccountingPolicyController.class)
class AccountingPolicyErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountingPolicyService accountingPolicyService;

    @Test
    void getAccountingPolicyById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(accountingPolicyService.getAccountingPolicyById(999L))
                .thenThrow(AccountingPolicyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(get("/api/accounting-policies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("AccountingPolicy not found with id: '999'"));
    }

    @Test
    void getAccountingPolicyByOrganizationAndYear_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(accountingPolicyService.getAccountingPolicyByOrganizationAndYear(1L, 2024))
                .thenThrow(AccountingPolicyNotFoundException.byOrganizationAndYear(1L, 2024));

        // When & Then
        mockMvc.perform(get("/api/accounting-policies/organization/1/year/2024"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("AccountingPolicy not found for organization 1 and year 2024"));
    }

    @Test
    void createAccountingPolicy_WhenOrganizationNotExists_ShouldReturn404() throws Exception {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(999L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(1L);

        when(accountingPolicyService.createAccountingPolicy(any(AccountingPolicyRequestDTO.class)))
                .thenThrow(OrganizationNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(post("/api/accounting-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Organization not found with id: '999'"));
    }

    @Test
    void createAccountingPolicy_WhenCurrencyNotExists_ShouldReturn404() throws Exception {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(999L);

        when(accountingPolicyService.createAccountingPolicy(any(AccountingPolicyRequestDTO.class)))
                .thenThrow(CurrencyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(post("/api/accounting-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Currency not found with id: '999'"));
    }

    @Test
    void createAccountingPolicy_WhenAlreadyExists_ShouldReturn409() throws Exception {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(1L);

        when(accountingPolicyService.createAccountingPolicy(any(AccountingPolicyRequestDTO.class)))
                .thenThrow(AccountingPolicyAlreadyExistsException.forOrganizationAndYear(1L, 2024));

        // When & Then
        mockMvc.perform(post("/api/accounting-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("AccountingPolicy already exists for organization 1 and year 2024"));
    }

    @Test
    void createAccountingPolicy_WhenInvalidYear_ShouldReturn400() throws Exception {
        // Given - year must be between 1900 and 2100
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(1800);
        requestDTO.setCurrencyId(1L);

        // When & Then
        mockMvc.perform(post("/api/accounting-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void createAccountingPolicy_WhenInvalidFiscalMonth_ShouldReturn400() throws Exception {
        // Given - fiscal month must be 1-12
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(1L);
        requestDTO.setFiscalYearStartMonth(13);

        // When & Then
        mockMvc.perform(post("/api/accounting-policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAccountingPolicy_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        AccountingPolicyRequestDTO requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(1L);

        when(accountingPolicyService.updateAccountingPolicy(anyLong(), any(AccountingPolicyRequestDTO.class)))
                .thenThrow(AccountingPolicyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(put("/api/accounting-policies/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateAccountingPolicy_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(accountingPolicyService.activateAccountingPolicy(999L))
                .thenThrow(AccountingPolicyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(patch("/api/accounting-policies/999/activate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateAccountingPolicy_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(accountingPolicyService.deactivateAccountingPolicy(999L))
                .thenThrow(AccountingPolicyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(patch("/api/accounting-policies/999/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAccountingPolicy_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        when(accountingPolicyService.getAccountingPolicyById(999L))
                .thenThrow(AccountingPolicyNotFoundException.byId(999L));

        // When & Then
        mockMvc.perform(delete("/api/accounting-policies/999"))
                .andExpect(status().isNotFound());
    }
}
package com.tarasantoniuk.finance.banking.bankaccount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountRequestDto;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountResponseDto;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.banking.bankaccount.service.BankAccountService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BankAccountService bankAccountService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getAllBankAccounts_ShouldReturnListOfBankAccounts() throws Exception {
        // Given
        BankAccountResponseDto account1 = new BankAccountResponseDto();
        account1.setId(1L);
        account1.setAccountNumber("UA213223130000026007233566001");
        account1.setHolderType(AccountHolderType.ORGANIZATION);

        BankAccountResponseDto account2 = new BankAccountResponseDto();
        account2.setId(2L);
        account2.setAccountNumber("UA213223130000026007233566002");
        account2.setHolderType(AccountHolderType.COUNTERPARTY);

        List<BankAccountResponseDto> accounts = Arrays.asList(account1, account2);
        when(bankAccountService.getAllBankAccounts()).thenReturn(accounts);

        // When & Then
        mockMvc.perform(get("/api/bank-accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].accountNumber").value("UA213223130000026007233566001"))
                .andExpect(jsonPath("$[1].accountNumber").value("UA213223130000026007233566002"));
    }

    @Test
    void getBankAccountById_WhenExists_ShouldReturnBankAccount() throws Exception {
        // Given
        BankAccountResponseDto account = new BankAccountResponseDto();
        account.setId(1L);
        account.setAccountNumber("UA213223130000026007233566001");
        account.setHolderType(AccountHolderType.ORGANIZATION);
        account.setHolderId(10L);

        when(bankAccountService.getBankAccountById(1L)).thenReturn(account);

        // When & Then
        mockMvc.perform(get("/api/bank-accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("UA213223130000026007233566001"))
                .andExpect(jsonPath("$.holderType").value("ORGANIZATION"));
    }

    @Test
    void getBankAccountByAccountNumber_WhenExists_ShouldReturnBankAccount() throws Exception {
        // Given
        BankAccountResponseDto account = new BankAccountResponseDto();
        account.setId(1L);
        account.setAccountNumber("UA213223130000026007233566001");

        when(bankAccountService.getBankAccountByAccountNumber("UA213223130000026007233566001"))
                .thenReturn(account);

        // When & Then
        mockMvc.perform(get("/api/bank-accounts/account-number/UA213223130000026007233566001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("UA213223130000026007233566001"));
    }

    @Test
    void getBankAccountsByHolder_ShouldReturnFilteredList() throws Exception {
        // Given
        BankAccountResponseDto account = new BankAccountResponseDto();
        account.setId(1L);
        account.setHolderType(AccountHolderType.ORGANIZATION);
        account.setHolderId(10L);

        when(bankAccountService.getBankAccountsByHolder(AccountHolderType.ORGANIZATION, 10L))
                .thenReturn(List.of(account));

        // When & Then
        mockMvc.perform(get("/api/bank-accounts/holder/ORGANIZATION/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].holderType").value("ORGANIZATION"))
                .andExpect(jsonPath("$[0].holderId").value(10));
    }

    @Test
    void getBankAccountsByBank_ShouldReturnFilteredList() throws Exception {
        // Given
        BankAccountResponseDto account = new BankAccountResponseDto();
        account.setId(1L);
        account.setAccountNumber("UA213223130000026007233566001");

        when(bankAccountService.getBankAccountsByBank(1L)).thenReturn(List.of(account));

        // When & Then
        mockMvc.perform(get("/api/bank-accounts/bank/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].accountNumber").value("UA213223130000026007233566001"));
    }

    @Test
    void getBankAccountsByStatus_ShouldReturnFilteredList() throws Exception {
        // Given
        BankAccountResponseDto account = new BankAccountResponseDto();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);

        when(bankAccountService.getBankAccountsByStatus(AccountStatus.ACTIVE))
                .thenReturn(List.of(account));

        // When & Then
        mockMvc.perform(get("/api/bank-accounts/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getDefaultBankAccountsByHolder_ShouldReturnDefaultAccounts() throws Exception {
        // Given
        BankAccountResponseDto account = new BankAccountResponseDto();
        account.setId(1L);
        account.setHolderType(AccountHolderType.ORGANIZATION);
        account.setHolderId(10L);
        account.setIsDefault(true);

        when(bankAccountService.getDefaultBankAccountsByHolder(AccountHolderType.ORGANIZATION, 10L))
                .thenReturn(List.of(account));

        // When & Then
        mockMvc.perform(get("/api/bank-accounts/holder/ORGANIZATION/10/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].isDefault").value(true));
    }

    @Test
    void createBankAccount_WhenValid_ShouldReturnCreated() throws Exception {
        // Given
        BankAccountRequestDto requestDTO = new BankAccountRequestDto();
        requestDTO.setAccountNumber("UA213223130000026007233566001");
        requestDTO.setHolderType(AccountHolderType.ORGANIZATION);
        requestDTO.setHolderId(10L);
        requestDTO.setBankId(1L);
        requestDTO.setCurrencyId(1L);
        requestDTO.setStatus(AccountStatus.ACTIVE);
        requestDTO.setIsDefault(true);

        BankAccountResponseDto responseDTO = new BankAccountResponseDto();
        responseDTO.setId(1L);
        responseDTO.setAccountNumber("UA213223130000026007233566001");
        responseDTO.setHolderType(AccountHolderType.ORGANIZATION);

        when(bankAccountService.createBankAccount(any(BankAccountRequestDto.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("UA213223130000026007233566001"));
    }

    @Test
    void createBankAccount_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Given - missing required fields
        BankAccountRequestDto requestDTO = new BankAccountRequestDto();

        // When & Then
        mockMvc.perform(post("/api/bank-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBankAccount_WhenValid_ShouldReturnUpdated() throws Exception {
        // Given
        BankAccountRequestDto requestDTO = new BankAccountRequestDto();
        requestDTO.setAccountNumber("UA213223130000026007233566001");
        requestDTO.setHolderType(AccountHolderType.ORGANIZATION);
        requestDTO.setHolderId(10L);
        requestDTO.setBankId(1L);
        requestDTO.setCurrencyId(1L);

        BankAccountResponseDto responseDTO = new BankAccountResponseDto();
        responseDTO.setId(1L);
        responseDTO.setAccountNumber("UA213223130000026007233566001");

        when(bankAccountService.updateBankAccount(anyLong(), any(BankAccountRequestDto.class)))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/bank-accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("UA213223130000026007233566001"));
    }

    @Test
    void changeStatus_WhenExists_ShouldReturnUpdated() throws Exception {
        // Given
        BankAccountResponseDto responseDTO = new BankAccountResponseDto();
        responseDTO.setId(1L);
        responseDTO.setStatus(AccountStatus.INACTIVE);

        when(bankAccountService.changeStatus(1L, AccountStatus.INACTIVE)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/bank-accounts/1/status/INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void setAsDefault_WhenExists_ShouldReturnUpdated() throws Exception {
        // Given
        BankAccountResponseDto responseDTO = new BankAccountResponseDto();
        responseDTO.setId(1L);
        responseDTO.setIsDefault(true);

        when(bankAccountService.setAsDefault(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/bank-accounts/1/set-default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void unsetAsDefault_WhenExists_ShouldReturnUpdated() throws Exception {
        // Given
        BankAccountResponseDto responseDTO = new BankAccountResponseDto();
        responseDTO.setId(1L);
        responseDTO.setIsDefault(false);

        when(bankAccountService.unsetAsDefault(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/bank-accounts/1/unset-default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDefault").value(false));
    }

    @Test
    void deleteBankAccount_WhenExists_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/bank-accounts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getBankAccountsByHolderType_ShouldReturnAccountsOfThatType() throws Exception {
        BankAccountResponseDto account = new BankAccountResponseDto();
        account.setId(1L);
        account.setHolderType(AccountHolderType.ORGANIZATION);
        when(bankAccountService.getBankAccountsByHolderType(AccountHolderType.ORGANIZATION))
                .thenReturn(List.of(account));

        mockMvc.perform(get("/api/bank-accounts/holder/ORGANIZATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].holderType").value("ORGANIZATION"));

        verify(bankAccountService).getBankAccountsByHolderType(AccountHolderType.ORGANIZATION);
    }
}

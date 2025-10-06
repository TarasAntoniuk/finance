package com.tarasantoniuk.finance.accountingpolicy.service;

import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyRequestDTO;
import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyResponseDTO;
import com.tarasantoniuk.finance.accountingpolicy.entity.AccountingPolicy;
import com.tarasantoniuk.finance.accountingpolicy.exception.AccountingPolicyAlreadyExistsException;
import com.tarasantoniuk.finance.accountingpolicy.exception.AccountingPolicyNotFoundException;
import com.tarasantoniuk.finance.accountingpolicy.mapper.AccountingPolicyMapper;
import com.tarasantoniuk.finance.accountingpolicy.repository.AccountingPolicyRepository;
import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.organization.entity.Organization;
import com.tarasantoniuk.finance.organization.exeption.OrganizationNotFoundException;
import com.tarasantoniuk.finance.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountingPolicyServiceTest {

    @Mock
    private AccountingPolicyRepository accountingPolicyRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private AccountingPolicyMapper accountingPolicyMapper;

    @InjectMocks
    private AccountingPolicyService accountingPolicyService;

    private AccountingPolicy accountingPolicy;
    private AccountingPolicyRequestDTO requestDTO;
    private AccountingPolicyResponseDTO responseDTO;
    private Organization organization;
    private Currency currency;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId(1L);
        organization.setName("Test Organization");

        currency = new Currency();
        currency.setId(1L);
        currency.setCode("UAH");

        accountingPolicy = new AccountingPolicy();
        accountingPolicy.setId(1L);
        accountingPolicy.setOrganization(organization);
        accountingPolicy.setYear(2024);
        accountingPolicy.setCurrency(currency);
        accountingPolicy.setFiscalYearStartMonth(1);
        accountingPolicy.setDepreciationMethod("STRAIGHT_LINE");
        accountingPolicy.setInventoryValuationMethod("FIFO");
        accountingPolicy.setRevenueRecognitionMethod("ACCRUAL");
        accountingPolicy.setVatAccountingMethod("INVOICE");
        accountingPolicy.setIsActive(true);

        requestDTO = new AccountingPolicyRequestDTO();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(1L);
        requestDTO.setFiscalYearStartMonth(1);
        requestDTO.setDepreciationMethod("STRAIGHT_LINE");
        requestDTO.setInventoryValuationMethod("FIFO");
        requestDTO.setRevenueRecognitionMethod("ACCRUAL");
        requestDTO.setVatAccountingMethod("INVOICE");

        responseDTO = new AccountingPolicyResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setYear(2024);
    }

    @Test
    void getAllAccountingPolicies_ShouldReturnListOfPolicies() {
        // Given
        List<AccountingPolicy> policies = Arrays.asList(accountingPolicy);
        when(accountingPolicyRepository.findAll()).thenReturn(policies);
        when(accountingPolicyMapper.toResponseDTOList(policies)).thenReturn(Arrays.asList(responseDTO));

        // When
        List<AccountingPolicyResponseDTO> result = accountingPolicyService.getAllAccountingPolicies();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountingPolicyRepository, times(1)).findAll();
    }

    @Test
    void getAccountingPolicyById_WhenExists_ShouldReturnPolicy() {
        // Given
        when(accountingPolicyRepository.findById(1L)).thenReturn(Optional.of(accountingPolicy));
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        // When
        AccountingPolicyResponseDTO result = accountingPolicyService.getAccountingPolicyById(1L);

        // Then
        assertNotNull(result);
        assertEquals(2024, result.getYear());
        verify(accountingPolicyRepository, times(1)).findById(1L);
    }

    @Test
    void getAccountingPolicyById_WhenNotExists_ShouldThrowException() {
        // Given
        when(accountingPolicyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AccountingPolicyNotFoundException.class,
                () -> accountingPolicyService.getAccountingPolicyById(1L));
    }

    @Test
    void getAccountingPolicyByOrganizationAndYear_WhenExists_ShouldReturnPolicy() {
        // Given
        when(accountingPolicyRepository.findByOrganizationIdAndYear(1L, 2024))
                .thenReturn(Optional.of(accountingPolicy));
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        // When
        AccountingPolicyResponseDTO result = accountingPolicyService
                .getAccountingPolicyByOrganizationAndYear(1L, 2024);

        // Then
        assertNotNull(result);
        assertEquals(2024, result.getYear());
    }

    @Test
    void getAccountingPolicyByOrganizationAndYear_WhenNotExists_ShouldThrowException() {
        // Given
        when(accountingPolicyRepository.findByOrganizationIdAndYear(1L, 2024))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(AccountingPolicyNotFoundException.class,
                () -> accountingPolicyService.getAccountingPolicyByOrganizationAndYear(1L, 2024));
    }

    @Test
    void getAccountingPoliciesByOrganization_ShouldReturnFilteredList() {
        // Given
        List<AccountingPolicy> policies = Arrays.asList(accountingPolicy);
        when(accountingPolicyRepository.findByOrganizationId(1L)).thenReturn(policies);
        when(accountingPolicyMapper.toResponseDTOList(policies)).thenReturn(Arrays.asList(responseDTO));

        // When
        List<AccountingPolicyResponseDTO> result = accountingPolicyService
                .getAccountingPoliciesByOrganization(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAccountingPoliciesByYear_ShouldReturnFilteredList() {
        // Given
        List<AccountingPolicy> policies = Arrays.asList(accountingPolicy);
        when(accountingPolicyRepository.findByYear(2024)).thenReturn(policies);
        when(accountingPolicyMapper.toResponseDTOList(policies)).thenReturn(Arrays.asList(responseDTO));

        // When
        List<AccountingPolicyResponseDTO> result = accountingPolicyService.getAccountingPoliciesByYear(2024);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAccountingPoliciesByYearRange_ShouldReturnFilteredList() {
        // Given
        List<AccountingPolicy> policies = Arrays.asList(accountingPolicy);
        when(accountingPolicyRepository.findByYearBetween(2020, 2024)).thenReturn(policies);
        when(accountingPolicyMapper.toResponseDTOList(policies)).thenReturn(Arrays.asList(responseDTO));

        // When
        List<AccountingPolicyResponseDTO> result = accountingPolicyService
                .getAccountingPoliciesByYearRange(2020, 2024);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void createAccountingPolicy_WhenValid_ShouldReturnCreatedPolicy() {
        // Given
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(accountingPolicyRepository.existsByOrganizationIdAndYear(1L, 2024)).thenReturn(false);
        when(accountingPolicyMapper.toEntity(requestDTO)).thenReturn(accountingPolicy);
        when(accountingPolicyRepository.save(accountingPolicy)).thenReturn(accountingPolicy);
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        // When
        AccountingPolicyResponseDTO result = accountingPolicyService.createAccountingPolicy(requestDTO);

        // Then
        assertNotNull(result);
        verify(accountingPolicyRepository, times(1)).save(any(AccountingPolicy.class));
    }

    @Test
    void createAccountingPolicy_WhenOrganizationNotExists_ShouldThrowException() {
        // Given
        when(organizationRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(OrganizationNotFoundException.class,
                () -> accountingPolicyService.createAccountingPolicy(requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void createAccountingPolicy_WhenCurrencyNotExists_ShouldThrowException() {
        // Given
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(CurrencyNotFoundException.class,
                () -> accountingPolicyService.createAccountingPolicy(requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void createAccountingPolicy_WhenAlreadyExists_ShouldThrowException() {
        // Given
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(accountingPolicyRepository.existsByOrganizationIdAndYear(1L, 2024)).thenReturn(true);

        // When & Then
        assertThrows(AccountingPolicyAlreadyExistsException.class,
                () -> accountingPolicyService.createAccountingPolicy(requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void updateAccountingPolicy_WhenValid_ShouldReturnUpdatedPolicy() {
        // Given
        when(accountingPolicyRepository.findById(1L)).thenReturn(Optional.of(accountingPolicy));
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(true);
//        when(accountingPolicyRepository.existsByOrganizationIdAndYear(1L, 2024)).thenReturn(false);
        when(accountingPolicyRepository.save(accountingPolicy)).thenReturn(accountingPolicy);
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        // When
        AccountingPolicyResponseDTO result = accountingPolicyService.updateAccountingPolicy(1L, requestDTO);

        // Then
        assertNotNull(result);
        verify(accountingPolicyRepository, times(1)).save(accountingPolicy);
    }

    @Test
    void activateAccountingPolicy_WhenExists_ShouldActivateSuccessfully() {
        // Given
        accountingPolicy.setIsActive(false);
        when(accountingPolicyRepository.findById(1L)).thenReturn(Optional.of(accountingPolicy));
        when(accountingPolicyRepository.save(accountingPolicy)).thenReturn(accountingPolicy);
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        // When
        AccountingPolicyResponseDTO result = accountingPolicyService.activateAccountingPolicy(1L);

        // Then
        assertTrue(accountingPolicy.getIsActive());
        verify(accountingPolicyRepository, times(1)).save(accountingPolicy);
    }

    @Test
    void deactivateAccountingPolicy_WhenExists_ShouldDeactivateSuccessfully() {
        // Given
        when(accountingPolicyRepository.findById(1L)).thenReturn(Optional.of(accountingPolicy));
        when(accountingPolicyRepository.save(accountingPolicy)).thenReturn(accountingPolicy);
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        // When
        AccountingPolicyResponseDTO result = accountingPolicyService.deactivateAccountingPolicy(1L);

        // Then
        assertFalse(accountingPolicy.getIsActive());
        verify(accountingPolicyRepository, times(1)).save(accountingPolicy);
    }

    @Test
    void deleteAccountingPolicy_WhenExists_ShouldDeleteSuccessfully() {
        // Given
        when(accountingPolicyRepository.existsById(1L)).thenReturn(true);

        // When
        accountingPolicyService.deleteAccountingPolicy(1L);

        // Then
        verify(accountingPolicyRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteAccountingPolicy_WhenNotExists_ShouldThrowException() {
        // Given
        when(accountingPolicyRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(AccountingPolicyNotFoundException.class,
                () -> accountingPolicyService.deleteAccountingPolicy(1L));
        verify(accountingPolicyRepository, never()).deleteById(anyLong());
    }
}
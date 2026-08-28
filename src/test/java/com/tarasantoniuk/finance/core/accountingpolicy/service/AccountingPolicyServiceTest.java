package com.tarasantoniuk.finance.core.accountingpolicy.service;

import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyRequestDto;
import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyResponseDto;
import com.tarasantoniuk.finance.core.accountingpolicy.entity.AccountingPolicy;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.DepreciationMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.InventoryValuationMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.RevenueRecognitionMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.enums.VatAccountingMethod;
import com.tarasantoniuk.finance.core.accountingpolicy.exception.AccountingPolicyAlreadyExistsException;
import com.tarasantoniuk.finance.core.accountingpolicy.exception.AccountingPolicyNotFoundException;
import com.tarasantoniuk.finance.core.accountingpolicy.mapper.AccountingPolicyMapper;
import com.tarasantoniuk.finance.core.accountingpolicy.repository.AccountingPolicyRepository;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.core.organization.exception.OrganizationNotFoundException;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
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

    @Mock
    private OrganizationSecurityContext orgContext;

    @InjectMocks
    private AccountingPolicyService accountingPolicyService;

    private AccountingPolicy accountingPolicy;
    private AccountingPolicyRequestDto requestDTO;
    private AccountingPolicyResponseDto responseDTO;
    private Organization organization;
    private Currency currency;

    @BeforeEach
    void setUp() {
        lenient().when(orgContext.isAdmin()).thenReturn(false);
        lenient().when(orgContext.getActiveOrganizationId()).thenReturn(1L);
        lenient().when(orgContext.resolveOrganizationId(any())).thenReturn(1L);

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
        accountingPolicy.setDepreciationMethod(DepreciationMethod.STRAIGHT_LINE);
        accountingPolicy.setInventoryValuationMethod(InventoryValuationMethod.FIFO);
        accountingPolicy.setRevenueRecognitionMethod(RevenueRecognitionMethod.ACCRUAL);
        accountingPolicy.setVatAccountingMethod(VatAccountingMethod.INVOICE);
        accountingPolicy.setIsActive(true);

        requestDTO = new AccountingPolicyRequestDto();
        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2024);
        requestDTO.setCurrencyId(1L);
        requestDTO.setFiscalYearStartMonth(1);
        requestDTO.setDepreciationMethod(DepreciationMethod.STRAIGHT_LINE);
        requestDTO.setInventoryValuationMethod(InventoryValuationMethod.FIFO);
        requestDTO.setRevenueRecognitionMethod(RevenueRecognitionMethod.ACCRUAL);
        requestDTO.setVatAccountingMethod(VatAccountingMethod.INVOICE);

        responseDTO = new AccountingPolicyResponseDto();
        responseDTO.setId(1L);
        responseDTO.setYear(2024);
    }

    @Test
    void getAllAccountingPolicies_ShouldReturnListOfPolicies() {
        List<AccountingPolicy> policies = Collections.singletonList(accountingPolicy);
        Page<AccountingPolicy> page = new PageImpl<>(policies);
        when(accountingPolicyRepository.findAllWithRelations(any(), any(Pageable.class))).thenReturn(page);
        when(accountingPolicyMapper.toResponseDTOList(policies)).thenReturn(Collections.singletonList(responseDTO));

        PageResponse<AccountingPolicyResponseDto> result = accountingPolicyService.getAllAccountingPolicies(0, 50);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(accountingPolicyRepository, times(1)).findAllWithRelations(any(), any(Pageable.class));
    }

    @Test
    void getAccountingPolicyById_WhenExists_ShouldReturnPolicy() {
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(accountingPolicy));
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        AccountingPolicyResponseDto result = accountingPolicyService.getAccountingPolicyById(1L);

        assertNotNull(result);
        assertEquals(2024, result.getYear());
        verify(accountingPolicyRepository, times(1)).findByIdWithRelations(1L);
    }

    @Test
    void getAccountingPolicyById_WhenNotExists_ShouldThrowException() {
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(AccountingPolicyNotFoundException.class,
                () -> accountingPolicyService.getAccountingPolicyById(1L));
    }

    @Test
    void getAccountingPolicyByOrganizationAndYear_WhenExists_ShouldReturnPolicy() {
        when(accountingPolicyRepository.findByOrganizationIdAndYearWithRelations(1L, 2024))
                .thenReturn(Optional.of(accountingPolicy));
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        AccountingPolicyResponseDto result = accountingPolicyService
                .getAccountingPolicyByOrganizationAndYear(1L, 2024);

        assertNotNull(result);
        assertEquals(2024, result.getYear());
    }

    @Test
    void getAccountingPolicyByOrganizationAndYear_WhenNotExists_ShouldThrowException() {
        when(accountingPolicyRepository.findByOrganizationIdAndYearWithRelations(1L, 2024))
                .thenReturn(Optional.empty());

        assertThrows(AccountingPolicyNotFoundException.class,
                () -> accountingPolicyService.getAccountingPolicyByOrganizationAndYear(1L, 2024));
    }

    @Test
    void getAccountingPoliciesByOrganization_ShouldReturnFilteredList() {
        List<AccountingPolicy> policies = Collections.singletonList(accountingPolicy);
        when(accountingPolicyRepository.findByOrganizationIdWithRelations(eq(1L), any(Pageable.class))).thenReturn(policies);
        when(accountingPolicyMapper.toResponseDTOList(policies)).thenReturn(Collections.singletonList(responseDTO));

        List<AccountingPolicyResponseDto> result = accountingPolicyService
                .getAccountingPoliciesByOrganization(1L, 500);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAccountingPoliciesByYear_ShouldReturnFilteredList() {
        List<AccountingPolicy> policies = Collections.singletonList(accountingPolicy);
        when(accountingPolicyRepository.findByYearWithRelations(eq(2024), any(), any(Pageable.class))).thenReturn(policies);
        when(accountingPolicyMapper.toResponseDTOList(policies)).thenReturn(Collections.singletonList(responseDTO));

        List<AccountingPolicyResponseDto> result = accountingPolicyService.getAccountingPoliciesByYear(2024, 500);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAccountingPoliciesByYearRange_ShouldReturnFilteredList() {
        List<AccountingPolicy> policies = Collections.singletonList(accountingPolicy);
        when(accountingPolicyRepository.findByYearBetweenWithRelations(eq(2020), eq(2024), any(), any(Pageable.class))).thenReturn(policies);
        when(accountingPolicyMapper.toResponseDTOList(policies)).thenReturn(Collections.singletonList(responseDTO));

        List<AccountingPolicyResponseDto> result = accountingPolicyService
                .getAccountingPoliciesByYearRange(2020, 2024, 500);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void createAccountingPolicy_WhenValid_ShouldReturnCreatedPolicy() {
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(accountingPolicyRepository.existsByOrganizationIdAndYear(1L, 2024)).thenReturn(false);
        when(accountingPolicyMapper.toEntity(requestDTO)).thenReturn(accountingPolicy);
        when(accountingPolicyRepository.save(accountingPolicy)).thenReturn(accountingPolicy);
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        AccountingPolicyResponseDto result = accountingPolicyService.createAccountingPolicy(requestDTO);

        assertNotNull(result);
        verify(accountingPolicyRepository, times(1)).save(any(AccountingPolicy.class));
    }

    @Test
    void createAccountingPolicy_WhenOrganizationNotExists_ShouldThrowException() {
        when(organizationRepository.existsById(1L)).thenReturn(false);

        assertThrows(OrganizationNotFoundException.class,
                () -> accountingPolicyService.createAccountingPolicy(requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void createAccountingPolicy_WhenCurrencyNotExists_ShouldThrowException() {
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(false);

        assertThrows(CurrencyNotFoundException.class,
                () -> accountingPolicyService.createAccountingPolicy(requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void createAccountingPolicy_WhenAlreadyExists_ShouldThrowException() {
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(accountingPolicyRepository.existsByOrganizationIdAndYear(1L, 2024)).thenReturn(true);

        assertThrows(AccountingPolicyAlreadyExistsException.class,
                () -> accountingPolicyService.createAccountingPolicy(requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void updateAccountingPolicy_WhenValid_ShouldReturnUpdatedPolicy() {
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(accountingPolicy));
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(accountingPolicyRepository.save(accountingPolicy)).thenReturn(accountingPolicy);
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        AccountingPolicyResponseDto result = accountingPolicyService.updateAccountingPolicy(1L, requestDTO);

        assertNotNull(result);
        verify(accountingPolicyRepository, times(1)).save(accountingPolicy);
    }

    @Test
    void updateAccountingPolicy_WhenNotExists_ShouldThrowException() {
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(AccountingPolicyNotFoundException.class,
                () -> accountingPolicyService.updateAccountingPolicy(1L, requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void updateAccountingPolicy_WhenOrganizationNotExists_ShouldThrowException() {
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(accountingPolicy));
        when(organizationRepository.existsById(1L)).thenReturn(false);

        assertThrows(OrganizationNotFoundException.class,
                () -> accountingPolicyService.updateAccountingPolicy(1L, requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void updateAccountingPolicy_WhenCurrencyNotExists_ShouldThrowException() {
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(accountingPolicy));
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(false);

        assertThrows(CurrencyNotFoundException.class,
                () -> accountingPolicyService.updateAccountingPolicy(1L, requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void updateAccountingPolicy_WhenChangingYearToExisting_ShouldThrowException() {
        accountingPolicy.getOrganization().setId(1L);
        accountingPolicy.setYear(2024);

        requestDTO.setOrganizationId(1L);
        requestDTO.setYear(2025);

        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(accountingPolicy));
        when(organizationRepository.existsById(1L)).thenReturn(true);
        when(currencyRepository.existsById(1L)).thenReturn(true);
        when(accountingPolicyRepository.existsByOrganizationIdAndYear(1L, 2025)).thenReturn(true);

        assertThrows(AccountingPolicyAlreadyExistsException.class,
                () -> accountingPolicyService.updateAccountingPolicy(1L, requestDTO));
        verify(accountingPolicyRepository, never()).save(any(AccountingPolicy.class));
    }

    @Test
    void activateAccountingPolicy_WhenExists_ShouldActivateSuccessfully() {
        accountingPolicy.setIsActive(false);
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(accountingPolicy));
        when(accountingPolicyRepository.save(accountingPolicy)).thenReturn(accountingPolicy);
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        accountingPolicyService.activateAccountingPolicy(1L);

        assertTrue(accountingPolicy.getIsActive());
        verify(accountingPolicyRepository, times(1)).save(accountingPolicy);
    }

    @Test
    void deactivateAccountingPolicy_WhenExists_ShouldDeactivateSuccessfully() {
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(accountingPolicy));
        when(accountingPolicyRepository.save(accountingPolicy)).thenReturn(accountingPolicy);
        when(accountingPolicyMapper.toResponseDTO(accountingPolicy)).thenReturn(responseDTO);

        accountingPolicyService.deactivateAccountingPolicy(1L);

        assertFalse(accountingPolicy.getIsActive());
        verify(accountingPolicyRepository, times(1)).save(accountingPolicy);
    }

    @Test
    void deleteAccountingPolicy_WhenExists_ShouldDeleteSuccessfully() {
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(accountingPolicy));

        accountingPolicyService.deleteAccountingPolicy(1L);

        verify(accountingPolicyRepository, times(1)).delete(accountingPolicy);
    }

    @Test
    void deleteAccountingPolicy_WhenNotExists_ShouldThrowException() {
        when(accountingPolicyRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(AccountingPolicyNotFoundException.class,
                () -> accountingPolicyService.deleteAccountingPolicy(1L));
        verify(accountingPolicyRepository, never()).delete(any(AccountingPolicy.class));
    }

    @Test
    void getActiveAccountingPoliciesByOrganization_ShouldReturnOnlyActivePolicies() {
        List<AccountingPolicy> activePolicies = Collections.singletonList(accountingPolicy);
        when(accountingPolicyRepository.findByOrganizationIdAndIsActiveWithRelations(eq(1L), eq(true), any(Pageable.class)))
                .thenReturn(activePolicies);
        when(accountingPolicyMapper.toResponseDTOList(activePolicies))
                .thenReturn(Collections.singletonList(responseDTO));

        List<AccountingPolicyResponseDto> result = accountingPolicyService
                .getActiveAccountingPoliciesByOrganization(1L, 500);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountingPolicyRepository, times(1)).findByOrganizationIdAndIsActiveWithRelations(eq(1L), eq(true), any(Pageable.class));
    }

    @Test
    void getActiveAccountingPoliciesByOrganization_WhenNoActivePolicies_ShouldReturnEmptyList() {
        when(accountingPolicyRepository.findByOrganizationIdAndIsActiveWithRelations(eq(1L), eq(true), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(accountingPolicyMapper.toResponseDTOList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<AccountingPolicyResponseDto> result = accountingPolicyService
                .getActiveAccountingPoliciesByOrganization(1L, 500);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(accountingPolicyRepository, times(1)).findByOrganizationIdAndIsActiveWithRelations(eq(1L), eq(true), any(Pageable.class));
    }

    @Test
    void getAccountingPoliciesByCurrency_ShouldReturnFilteredList() {
        List<AccountingPolicy> policies = Collections.singletonList(accountingPolicy);
        when(accountingPolicyRepository.findByCurrencyIdWithRelations(eq(1L), any(), any(Pageable.class))).thenReturn(policies);
        when(accountingPolicyMapper.toResponseDTOList(policies))
                .thenReturn(Collections.singletonList(responseDTO));

        List<AccountingPolicyResponseDto> result = accountingPolicyService
                .getAccountingPoliciesByCurrency(1L, 500);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountingPolicyRepository, times(1)).findByCurrencyIdWithRelations(eq(1L), any(), any(Pageable.class));
    }

    @Test
    void getAccountingPoliciesByCurrency_WhenNoPolicies_ShouldReturnEmptyList() {
        when(accountingPolicyRepository.findByCurrencyIdWithRelations(eq(1L), any(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(accountingPolicyMapper.toResponseDTOList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<AccountingPolicyResponseDto> result = accountingPolicyService
                .getAccountingPoliciesByCurrency(1L, 500);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(accountingPolicyRepository, times(1)).findByCurrencyIdWithRelations(eq(1L), any(), any(Pageable.class));
    }

    @Test
    void getAllAccountingPolicies_WhenAdmin_ShouldQueryWithoutOrganizationScope() {
        when(orgContext.isAdmin()).thenReturn(true);
        when(accountingPolicyRepository.findAllWithRelations(eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(accountingPolicyMapper.toResponseDTOList(List.of())).thenReturn(List.of());

        accountingPolicyService.getAllAccountingPolicies(0, 50);

        verify(accountingPolicyRepository).findAllWithRelations(eq(null), any(Pageable.class));
        verify(orgContext, never()).getActiveOrganizationId();
    }
}

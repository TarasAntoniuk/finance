package com.tarasantoniuk.finance.core.accountingpolicy.service;

import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyRequestDto;
import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyResponseDto;
import com.tarasantoniuk.finance.core.accountingpolicy.entity.AccountingPolicy;
import com.tarasantoniuk.finance.core.accountingpolicy.exception.AccountingPolicyAlreadyExistsException;
import com.tarasantoniuk.finance.core.accountingpolicy.exception.AccountingPolicyNotFoundException;
import com.tarasantoniuk.finance.core.accountingpolicy.mapper.AccountingPolicyMapper;
import com.tarasantoniuk.finance.core.accountingpolicy.repository.AccountingPolicyRepository;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.exception.OrganizationNotFoundException;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountingPolicyService {

    private final AccountingPolicyRepository accountingPolicyRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrencyRepository currencyRepository;
    private final AccountingPolicyMapper accountingPolicyMapper;

    public AccountingPolicyService(AccountingPolicyRepository accountingPolicyRepository,
                                   OrganizationRepository organizationRepository,
                                   CurrencyRepository currencyRepository,
                                   AccountingPolicyMapper accountingPolicyMapper) {
        this.accountingPolicyRepository = accountingPolicyRepository;
        this.organizationRepository = organizationRepository;
        this.currencyRepository = currencyRepository;
        this.accountingPolicyMapper = accountingPolicyMapper;
    }

    /**
     * Get all accounting policies with pagination and optimized query (solves N+1 problem).
     * Uses JOIN FETCH to load organization and currency in a single query.
     */
    public PageResponse<AccountingPolicyResponseDto> getAllAccountingPolicies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountingPolicy> policyPage = accountingPolicyRepository.findAllWithRelations(pageable);

        List<AccountingPolicyResponseDto> dtos = accountingPolicyMapper.toResponseDTOList(policyPage.getContent());

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(policyPage.getNumber())
                .totalPages(policyPage.getTotalPages())
                .pageSize(policyPage.getSize())
                .totalElements(policyPage.getTotalElements())
                .hasNext(policyPage.hasNext())
                .hasPrevious(policyPage.hasPrevious())
                .build();

        return PageResponse.<AccountingPolicyResponseDto>builder()
                .content(dtos)
                .metadata(metadata)
                .build();
    }

    /**
     * Get accounting policy by ID with optimized query.
     */
    public AccountingPolicyResponseDto getAccountingPolicyById(Long id) {
        AccountingPolicy policy = accountingPolicyRepository.findByIdWithRelations(id)
                .orElseThrow(() -> AccountingPolicyNotFoundException.byId(id));
        return accountingPolicyMapper.toResponseDTO(policy);
    }

    /**
     * Get accounting policy by organization and year with optimized query.
     */
    public AccountingPolicyResponseDto getAccountingPolicyByOrganizationAndYear(Long organizationId, Integer year) {
        AccountingPolicy policy = accountingPolicyRepository.findByOrganizationIdAndYearWithRelations(organizationId, year)
                .orElseThrow(() -> AccountingPolicyNotFoundException.byOrganizationAndYear(organizationId, year));
        return accountingPolicyMapper.toResponseDTO(policy);
    }

    /**
     * Get accounting policies by organization with optimized query.
     */
    public List<AccountingPolicyResponseDto> getAccountingPoliciesByOrganization(Long organizationId) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByOrganizationIdWithRelations(organizationId);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    /**
     * Get accounting policies by year with optimized query.
     */
    public List<AccountingPolicyResponseDto> getAccountingPoliciesByYear(Integer year) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByYearWithRelations(year);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    /**
     * Get active accounting policies by organization with optimized query.
     */
    public List<AccountingPolicyResponseDto> getActiveAccountingPoliciesByOrganization(Long organizationId) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByOrganizationIdAndIsActiveWithRelations(organizationId, true);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    /**
     * Get accounting policies by currency with optimized query.
     */
    public List<AccountingPolicyResponseDto> getAccountingPoliciesByCurrency(Long currencyId) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByCurrencyIdWithRelations(currencyId);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    /**
     * Get accounting policies by year range with optimized query.
     */
    public List<AccountingPolicyResponseDto> getAccountingPoliciesByYearRange(Integer startYear, Integer endYear) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByYearBetweenWithRelations(startYear, endYear);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    @Transactional
    public AccountingPolicyResponseDto createAccountingPolicy(AccountingPolicyRequestDto requestDTO) {
        if (!organizationRepository.existsById(requestDTO.getOrganizationId())) {
            throw OrganizationNotFoundException.byId(requestDTO.getOrganizationId());
        }

        if (!currencyRepository.existsById(requestDTO.getCurrencyId())) {
            throw CurrencyNotFoundException.byId(requestDTO.getCurrencyId());
        }

        if (accountingPolicyRepository.existsByOrganizationIdAndYear(
                requestDTO.getOrganizationId(), requestDTO.getYear())) {
            throw AccountingPolicyAlreadyExistsException.forOrganizationAndYear(
                    requestDTO.getOrganizationId(), requestDTO.getYear());
        }

        AccountingPolicy policy = accountingPolicyMapper.toEntity(requestDTO);
        AccountingPolicy savedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(savedPolicy);
    }

    @Transactional
    public AccountingPolicyResponseDto updateAccountingPolicy(Long id, AccountingPolicyRequestDto requestDTO) {
        AccountingPolicy policy = accountingPolicyRepository.findByIdWithRelations(id)
                .orElseThrow(() -> AccountingPolicyNotFoundException.byId(id));

        if (!organizationRepository.existsById(requestDTO.getOrganizationId())) {
            throw OrganizationNotFoundException.byId(requestDTO.getOrganizationId());
        }

        if (!currencyRepository.existsById(requestDTO.getCurrencyId())) {
            throw CurrencyNotFoundException.byId(requestDTO.getCurrencyId());
        }

        if ((!policy.getOrganization().getId().equals(requestDTO.getOrganizationId())
                || !policy.getYear().equals(requestDTO.getYear()))
                && accountingPolicyRepository.existsByOrganizationIdAndYear(
                requestDTO.getOrganizationId(), requestDTO.getYear())) {
            throw AccountingPolicyAlreadyExistsException.forOrganizationAndYear(
                    requestDTO.getOrganizationId(), requestDTO.getYear());
        }

        accountingPolicyMapper.updateEntityFromDTO(requestDTO, policy);
        AccountingPolicy updatedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(updatedPolicy);
    }

    @Transactional
    public void deleteAccountingPolicy(Long id) {
        if (!accountingPolicyRepository.existsById(id)) {
            throw AccountingPolicyNotFoundException.byId(id);
        }
        accountingPolicyRepository.deleteById(id);
    }

    @Transactional
    public AccountingPolicyResponseDto deactivateAccountingPolicy(Long id) {
        AccountingPolicy policy = accountingPolicyRepository.findByIdWithRelations(id)
                .orElseThrow(() -> AccountingPolicyNotFoundException.byId(id));
        policy.setIsActive(false);
        AccountingPolicy updatedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(updatedPolicy);
    }

    @Transactional
    public AccountingPolicyResponseDto activateAccountingPolicy(Long id) {
        AccountingPolicy policy = accountingPolicyRepository.findByIdWithRelations(id)
                .orElseThrow(() -> AccountingPolicyNotFoundException.byId(id));
        policy.setIsActive(true);
        AccountingPolicy updatedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(updatedPolicy);
    }
}
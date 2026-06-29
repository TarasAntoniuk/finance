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
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
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
    private final OrganizationSecurityContext orgContext;

    public AccountingPolicyService(AccountingPolicyRepository accountingPolicyRepository,
                                   OrganizationRepository organizationRepository,
                                   CurrencyRepository currencyRepository,
                                   AccountingPolicyMapper accountingPolicyMapper,
                                   OrganizationSecurityContext orgContext) {
        this.accountingPolicyRepository = accountingPolicyRepository;
        this.organizationRepository = organizationRepository;
        this.currencyRepository = currencyRepository;
        this.accountingPolicyMapper = accountingPolicyMapper;
        this.orgContext = orgContext;
    }

    private Long queryOrgScope() {
        return orgContext.isAdmin() ? null : orgContext.getActiveOrganizationId();
    }

    public PageResponse<AccountingPolicyResponseDto> getAllAccountingPolicies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountingPolicy> policyPage = accountingPolicyRepository.findAllWithRelations(queryOrgScope(), pageable);

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

    public AccountingPolicyResponseDto getAccountingPolicyById(Long id) {
        AccountingPolicy policy = findEntityByIdOrThrow(id);
        return accountingPolicyMapper.toResponseDTO(policy);
    }

    public AccountingPolicyResponseDto getAccountingPolicyByOrganizationAndYear(Long organizationId, Integer year) {
        Long scopedOrgId = orgContext.resolveOrganizationId(organizationId);
        AccountingPolicy policy = accountingPolicyRepository.findByOrganizationIdAndYearWithRelations(scopedOrgId, year)
                .orElseThrow(() -> AccountingPolicyNotFoundException.byOrganizationAndYear(scopedOrgId, year));
        return accountingPolicyMapper.toResponseDTO(policy);
    }

    public List<AccountingPolicyResponseDto> getAccountingPoliciesByOrganization(Long organizationId, int limit) {
        Long scopedOrgId = orgContext.resolveOrganizationId(organizationId);
        List<AccountingPolicy> policies = accountingPolicyRepository.findByOrganizationIdWithRelations(scopedOrgId, PageRequest.of(0, limit));
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    public List<AccountingPolicyResponseDto> getAccountingPoliciesByYear(Integer year, int limit) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByYearWithRelations(year, queryOrgScope(), PageRequest.of(0, limit));
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    public List<AccountingPolicyResponseDto> getActiveAccountingPoliciesByOrganization(Long organizationId, int limit) {
        Long scopedOrgId = orgContext.resolveOrganizationId(organizationId);
        List<AccountingPolicy> policies = accountingPolicyRepository.findByOrganizationIdAndIsActiveWithRelations(scopedOrgId, true, PageRequest.of(0, limit));
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    public List<AccountingPolicyResponseDto> getAccountingPoliciesByCurrency(Long currencyId, int limit) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByCurrencyIdWithRelations(currencyId, queryOrgScope(), PageRequest.of(0, limit));
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    public List<AccountingPolicyResponseDto> getAccountingPoliciesByYearRange(Integer startYear, Integer endYear, int limit) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByYearBetweenWithRelations(startYear, endYear, queryOrgScope(), PageRequest.of(0, limit));
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    @Transactional
    public AccountingPolicyResponseDto createAccountingPolicy(AccountingPolicyRequestDto requestDTO) {
        Long scopedOrgId = orgContext.resolveOrganizationId(requestDTO.getOrganizationId());
        requestDTO.setOrganizationId(scopedOrgId);

        if (!organizationRepository.existsById(scopedOrgId)) {
            throw OrganizationNotFoundException.byId(scopedOrgId);
        }

        if (!currencyRepository.existsById(requestDTO.getCurrencyId())) {
            throw CurrencyNotFoundException.byId(requestDTO.getCurrencyId());
        }

        if (accountingPolicyRepository.existsByOrganizationIdAndYear(scopedOrgId, requestDTO.getYear())) {
            throw AccountingPolicyAlreadyExistsException.forOrganizationAndYear(scopedOrgId, requestDTO.getYear());
        }

        AccountingPolicy policy = accountingPolicyMapper.toEntity(requestDTO);
        AccountingPolicy savedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(savedPolicy);
    }

    @Transactional
    public AccountingPolicyResponseDto updateAccountingPolicy(Long id, AccountingPolicyRequestDto requestDTO) {
        AccountingPolicy policy = findEntityByIdOrThrow(id);

        Long scopedOrgId = orgContext.resolveOrganizationId(requestDTO.getOrganizationId());
        requestDTO.setOrganizationId(scopedOrgId);

        if (!organizationRepository.existsById(scopedOrgId)) {
            throw OrganizationNotFoundException.byId(scopedOrgId);
        }

        if (!currencyRepository.existsById(requestDTO.getCurrencyId())) {
            throw CurrencyNotFoundException.byId(requestDTO.getCurrencyId());
        }

        if ((!policy.getOrganization().getId().equals(scopedOrgId)
                || !policy.getYear().equals(requestDTO.getYear()))
                && accountingPolicyRepository.existsByOrganizationIdAndYear(scopedOrgId, requestDTO.getYear())) {
            throw AccountingPolicyAlreadyExistsException.forOrganizationAndYear(scopedOrgId, requestDTO.getYear());
        }

        accountingPolicyMapper.updateEntityFromDTO(requestDTO, policy);
        AccountingPolicy updatedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(updatedPolicy);
    }

    @Transactional
    public void deleteAccountingPolicy(Long id) {
        AccountingPolicy policy = findEntityByIdOrThrow(id);
        accountingPolicyRepository.delete(policy);
    }

    @Transactional
    public AccountingPolicyResponseDto deactivateAccountingPolicy(Long id) {
        AccountingPolicy policy = findEntityByIdOrThrow(id);
        policy.setIsActive(false);
        AccountingPolicy updatedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(updatedPolicy);
    }

    @Transactional
    public AccountingPolicyResponseDto activateAccountingPolicy(Long id) {
        AccountingPolicy policy = findEntityByIdOrThrow(id);
        policy.setIsActive(true);
        AccountingPolicy updatedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(updatedPolicy);
    }

    private AccountingPolicy findEntityByIdOrThrow(Long id) {
        AccountingPolicy policy = accountingPolicyRepository.findByIdWithRelations(id)
                .orElseThrow(() -> AccountingPolicyNotFoundException.byId(id));
        orgContext.validateAccess(policy.getOrganization().getId());
        return policy;
    }
}

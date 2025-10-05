package com.tarasantoniuk.finance.accountingpolicy.service;

import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyRequestDTO;
import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyResponseDTO;
import com.tarasantoniuk.finance.accountingpolicy.entity.AccountingPolicy;
import com.tarasantoniuk.finance.accountingpolicy.mapper.AccountingPolicyMapper;
import com.tarasantoniuk.finance.accountingpolicy.repository.AccountingPolicyRepository;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
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

    @Transactional(readOnly = true)
    public List<AccountingPolicyResponseDTO> getAllAccountingPolicies() {
        List<AccountingPolicy> policies = accountingPolicyRepository.findAll();
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    @Transactional(readOnly = true)
    public AccountingPolicyResponseDTO getAccountingPolicyById(Long id) {
        AccountingPolicy policy = accountingPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accounting policy not found with id: " + id));
        return accountingPolicyMapper.toResponseDTO(policy);
    }

    @Transactional(readOnly = true)
    public AccountingPolicyResponseDTO getAccountingPolicyByOrganizationAndYear(Long organizationId, Integer year) {
        AccountingPolicy policy = accountingPolicyRepository.findByOrganizationIdAndYear(organizationId, year)
                .orElseThrow(() -> new RuntimeException(
                        "Accounting policy not found for organization " + organizationId + " and year " + year));
        return accountingPolicyMapper.toResponseDTO(policy);
    }

    @Transactional(readOnly = true)
    public List<AccountingPolicyResponseDTO> getAccountingPoliciesByOrganization(Long organizationId) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByOrganizationId(organizationId);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    @Transactional(readOnly = true)
    public List<AccountingPolicyResponseDTO> getAccountingPoliciesByYear(Integer year) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByYear(year);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    @Transactional(readOnly = true)
    public List<AccountingPolicyResponseDTO> getActiveAccountingPoliciesByOrganization(Long organizationId) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByOrganizationIdAndIsActive(organizationId, true);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    @Transactional(readOnly = true)
    public List<AccountingPolicyResponseDTO> getAccountingPoliciesByCurrency(Long currencyId) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByCurrencyId(currencyId);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    @Transactional(readOnly = true)
    public List<AccountingPolicyResponseDTO> getAccountingPoliciesByYearRange(Integer startYear, Integer endYear) {
        List<AccountingPolicy> policies = accountingPolicyRepository.findByYearBetween(startYear, endYear);
        return accountingPolicyMapper.toResponseDTOList(policies);
    }

    @Transactional
    public AccountingPolicyResponseDTO createAccountingPolicy(AccountingPolicyRequestDTO requestDTO) {
        if (!organizationRepository.existsById(requestDTO.getOrganizationId())) {
            throw new RuntimeException("Organization not found with id: " + requestDTO.getOrganizationId());
        }

        if (!currencyRepository.existsById(requestDTO.getCurrencyId())) {
            throw new RuntimeException("Currency not found with id: " + requestDTO.getCurrencyId());
        }

        if (accountingPolicyRepository.existsByOrganizationIdAndYear(
                requestDTO.getOrganizationId(), requestDTO.getYear())) {
            throw new RuntimeException("Accounting policy already exists for organization "
                    + requestDTO.getOrganizationId() + " and year " + requestDTO.getYear());
        }

        AccountingPolicy policy = accountingPolicyMapper.toEntity(requestDTO);
        AccountingPolicy savedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(savedPolicy);
    }

    @Transactional
    public AccountingPolicyResponseDTO updateAccountingPolicy(Long id, AccountingPolicyRequestDTO requestDTO) {
        AccountingPolicy policy = accountingPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accounting policy not found with id: " + id));

        if (!organizationRepository.existsById(requestDTO.getOrganizationId())) {
            throw new RuntimeException("Organization not found with id: " + requestDTO.getOrganizationId());
        }

        if (!currencyRepository.existsById(requestDTO.getCurrencyId())) {
            throw new RuntimeException("Currency not found with id: " + requestDTO.getCurrencyId());
        }

        // Check if trying to change organization or year to existing combination
        if ((!policy.getOrganization().getId().equals(requestDTO.getOrganizationId())
                || !policy.getYear().equals(requestDTO.getYear()))
                && accountingPolicyRepository.existsByOrganizationIdAndYear(
                requestDTO.getOrganizationId(), requestDTO.getYear())) {
            throw new RuntimeException("Accounting policy already exists for organization "
                    + requestDTO.getOrganizationId() + " and year " + requestDTO.getYear());
        }

        accountingPolicyMapper.updateEntityFromDTO(requestDTO, policy);
        AccountingPolicy updatedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(updatedPolicy);
    }

    @Transactional
    public void deleteAccountingPolicy(Long id) {
        if (!accountingPolicyRepository.existsById(id)) {
            throw new RuntimeException("Accounting policy not found with id: " + id);
        }
        accountingPolicyRepository.deleteById(id);
    }

    @Transactional
    public AccountingPolicyResponseDTO deactivateAccountingPolicy(Long id) {
        AccountingPolicy policy = accountingPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accounting policy not found with id: " + id));
        policy.setIsActive(false);
        AccountingPolicy updatedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(updatedPolicy);
    }

    @Transactional
    public AccountingPolicyResponseDTO activateAccountingPolicy(Long id) {
        AccountingPolicy policy = accountingPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Accounting policy not found with id: " + id));
        policy.setIsActive(true);
        AccountingPolicy updatedPolicy = accountingPolicyRepository.save(policy);
        return accountingPolicyMapper.toResponseDTO(updatedPolicy);
    }
}
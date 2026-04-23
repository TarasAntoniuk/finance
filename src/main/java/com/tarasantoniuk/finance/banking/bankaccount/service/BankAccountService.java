package com.tarasantoniuk.finance.banking.bankaccount.service;


import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountRequestDto;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountResponseDto;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.banking.bankaccount.exception.BankAccountNotFoundException;
import com.tarasantoniuk.finance.banking.bankaccount.exception.DuplicateBankAccountException;
import com.tarasantoniuk.finance.banking.bankaccount.mapper.BankAccountMapper;
import com.tarasantoniuk.finance.banking.bankaccount.repository.BankAccountRepository;
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountMapper bankAccountMapper;
    private final OrganizationSecurityContext orgContext;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                              BankAccountMapper bankAccountMapper,
                              OrganizationSecurityContext orgContext) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountMapper = bankAccountMapper;
        this.orgContext = orgContext;
    }

    /**
     * Returns the organizationId to apply as a query filter.
     * Admin sees all orgs (null = bypass filter); non-admin is scoped to their own org.
     */
    private Long queryOrgScope() {
        return orgContext.isAdmin() ? null : orgContext.getActiveOrganizationId();
    }

    /**
     * Validates that the caller may access this account.
     * Organization-owned accounts: caller must belong to the owning org (admin bypasses).
     * Counterparty-owned accounts: visible to any authenticated user (shared directory).
     */
    private void validateAccountAccess(BankAccount account) {
        if (account.getHolderType() == AccountHolderType.ORGANIZATION) {
            orgContext.validateAccess(account.getHolderId());
        }
    }

    public List<BankAccountResponseDto> getAllBankAccounts() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAllWithRelations(queryOrgScope());
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    public BankAccountResponseDto getBankAccountById(Long id) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        validateAccountAccess(bankAccount);
        return bankAccountMapper.toResponse(bankAccount);
    }

    public BankAccountResponseDto getBankAccountByAccountNumber(String accountNumber) {
        BankAccount bankAccount = bankAccountRepository.findByAccountNumberWithRelations(accountNumber)
                .orElseThrow(() -> BankAccountNotFoundException.byAccountNumber(accountNumber));
        validateAccountAccess(bankAccount);
        return bankAccountMapper.toResponse(bankAccount);
    }

    /**
     * Get all bank accounts by holder type (all organizations or all counterparties).
     */
    public List<BankAccountResponseDto> getBankAccountsByHolderType(AccountHolderType holderType) {
        List<BankAccount> bankAccounts;
        if (holderType == AccountHolderType.ORGANIZATION) {
            bankAccounts = bankAccountRepository.findOrganizationAccountsWithRelations(queryOrgScope());
        } else {
            bankAccounts = bankAccountRepository.findCounterpartyAccountsWithRelations();
        }
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    public List<BankAccountResponseDto> getBankAccountsByHolder(AccountHolderType holderType, Long holderId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByHolderWithRelations(
                holderType, holderId, queryOrgScope());
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    public List<BankAccountResponseDto> getBankAccountsByBank(Long bankId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByBankIdWithRelations(bankId, queryOrgScope());
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    public List<BankAccountResponseDto> getBankAccountsByStatus(AccountStatus status) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByStatusWithRelations(status, queryOrgScope());
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    public List<BankAccountResponseDto> getDefaultBankAccountsByHolder(AccountHolderType holderType, Long holderId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findDefaultByHolderWithRelations(
                holderType, holderId, queryOrgScope());
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    @Transactional
    public BankAccountResponseDto createBankAccount(BankAccountRequestDto requestDTO) {
        if (requestDTO.getHolderType() == AccountHolderType.ORGANIZATION) {
            requestDTO.setHolderId(orgContext.resolveOrganizationId(requestDTO.getHolderId()));
        }

        if (bankAccountRepository.findByAccountNumber(requestDTO.getAccountNumber()).isPresent()) {
            throw DuplicateBankAccountException.byAccountNumber(requestDTO.getAccountNumber());
        }

        BankAccount bankAccount = bankAccountMapper.toEntity(requestDTO);
        BankAccount savedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(savedBankAccount);
    }

    @Transactional
    public BankAccountResponseDto updateBankAccount(Long id, BankAccountRequestDto requestDTO) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        validateAccountAccess(bankAccount);

        if (requestDTO.getHolderType() == AccountHolderType.ORGANIZATION) {
            requestDTO.setHolderId(orgContext.resolveOrganizationId(requestDTO.getHolderId()));
        }

        bankAccountRepository.findByAccountNumber(requestDTO.getAccountNumber())
                .ifPresent(existingBankAccount -> {
                    if (!existingBankAccount.getId().equals(id)) {
                        throw DuplicateBankAccountException.byAccountNumber(requestDTO.getAccountNumber());
                    }
                });

        bankAccountMapper.updateEntity(requestDTO, bankAccount);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(updatedBankAccount);
    }

    @Transactional
    public BankAccountResponseDto changeStatus(Long id, AccountStatus status) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        validateAccountAccess(bankAccount);
        bankAccount.setStatus(status);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(updatedBankAccount);
    }

    @Transactional
    public BankAccountResponseDto setAsDefault(Long id) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        validateAccountAccess(bankAccount);
        bankAccount.setIsDefault(true);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(updatedBankAccount);
    }

    @Transactional
    public BankAccountResponseDto unsetAsDefault(Long id) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        validateAccountAccess(bankAccount);
        bankAccount.setIsDefault(false);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(updatedBankAccount);
    }

    @Transactional
    public void deleteBankAccount(Long id) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        validateAccountAccess(bankAccount);
        bankAccountRepository.delete(bankAccount);
    }
}

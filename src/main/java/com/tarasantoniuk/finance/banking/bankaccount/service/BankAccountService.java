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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final BankAccountMapper bankAccountMapper;

    public BankAccountService(BankAccountRepository bankAccountRepository, BankAccountMapper bankAccountMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountMapper = bankAccountMapper;
    }

    /**
     * Get all bank accounts with optimized query (solves N+1 problem).
     * Uses JOIN FETCH to load bank and currency in a single query.
     */
    public List<BankAccountResponseDto> getAllBankAccounts() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAllWithRelations();
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    /**
     * Get bank account by ID with optimized query.
     */
    public BankAccountResponseDto getBankAccountById(Long id) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        return bankAccountMapper.toResponse(bankAccount);
    }

    /**
     * Get bank account by account number with optimized query.
     */
    public BankAccountResponseDto getBankAccountByAccountNumber(String accountNumber) {
        BankAccount bankAccount = bankAccountRepository.findByAccountNumberWithRelations(accountNumber)
                .orElseThrow(() -> BankAccountNotFoundException.byAccountNumber(accountNumber));
        return bankAccountMapper.toResponse(bankAccount);
    }

    /**
     * Get all bank accounts by holder type (all organizations or all counterparties).
     * Returns all accounts matching the holder type across all holders.
     */
    public List<BankAccountResponseDto> getBankAccountsByHolderType(AccountHolderType holderType) {
        List<BankAccount> bankAccounts;
        if (holderType == AccountHolderType.ORGANIZATION) {
            bankAccounts = bankAccountRepository.findOrganizationAccountsWithRelations();
        } else {
            bankAccounts = bankAccountRepository.findCounterpartyAccountsWithRelations();
        }
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    /**
     * Get bank accounts by holder with optimized query.
     */
    public List<BankAccountResponseDto> getBankAccountsByHolder(AccountHolderType holderType, Long holderId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByHolderWithRelations(holderType, holderId);
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    /**
     * Get bank accounts by bank with optimized query.
     */
    public List<BankAccountResponseDto> getBankAccountsByBank(Long bankId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByBankIdWithRelations(bankId);
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    /**
     * Get bank accounts by status with optimized query.
     */
    public List<BankAccountResponseDto> getBankAccountsByStatus(AccountStatus status) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByStatusWithRelations(status);
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    /**
     * Get default bank accounts by holder with optimized query.
     */
    public List<BankAccountResponseDto> getDefaultBankAccountsByHolder(AccountHolderType holderType, Long holderId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findDefaultByHolderWithRelations(holderType, holderId);
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    @Transactional
    public BankAccountResponseDto createBankAccount(BankAccountRequestDto requestDTO) {
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
        bankAccount.setStatus(status);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(updatedBankAccount);
    }

    @Transactional
    public BankAccountResponseDto setAsDefault(Long id) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        bankAccount.setIsDefault(true);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(updatedBankAccount);
    }

    @Transactional
    public BankAccountResponseDto unsetAsDefault(Long id) {
        BankAccount bankAccount = bankAccountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        bankAccount.setIsDefault(false);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(updatedBankAccount);
    }

    @Transactional
    public void deleteBankAccount(Long id) {
        if (!bankAccountRepository.existsById(id)) {
            throw BankAccountNotFoundException.byId(id);
        }
        bankAccountRepository.deleteById(id);
    }
}
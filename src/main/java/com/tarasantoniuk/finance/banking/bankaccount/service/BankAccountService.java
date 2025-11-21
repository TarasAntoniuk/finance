package com.tarasantoniuk.finance.banking.bankaccount.service;

import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountRequestDTO;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountResponseDTO;
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

    public List<BankAccountResponseDTO> getAllBankAccounts() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    public BankAccountResponseDTO getBankAccountById(Long id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        return bankAccountMapper.toResponse(bankAccount);
    }

    public BankAccountResponseDTO getBankAccountByAccountNumber(String accountNumber) {
        BankAccount bankAccount = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> BankAccountNotFoundException.byAccountNumber(accountNumber));
        return bankAccountMapper.toResponse(bankAccount);
    }

    public List<BankAccountResponseDTO> getBankAccountsByHolder(AccountHolderType holderType, Long holderId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByHolderTypeAndHolderId(holderType, holderId);
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    public List<BankAccountResponseDTO> getBankAccountsByBank(Long bankId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByBankId(bankId);
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    public List<BankAccountResponseDTO> getBankAccountsByStatus(AccountStatus status) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByStatus(status);
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    public List<BankAccountResponseDTO> getDefaultBankAccountsByHolder(AccountHolderType holderType, Long holderId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findByHolderTypeAndHolderIdAndIsDefaultTrue(holderType, holderId);
        return bankAccountMapper.toResponseList(bankAccounts);
    }

    @Transactional
    public BankAccountResponseDTO createBankAccount(BankAccountRequestDTO requestDTO) {
        // Check if bank account with this account number already exists
        if (bankAccountRepository.findByAccountNumber(requestDTO.getAccountNumber()).isPresent()) {
            throw DuplicateBankAccountException.byAccountNumber(requestDTO.getAccountNumber());
        }

        BankAccount bankAccount = bankAccountMapper.toEntity(requestDTO);
        BankAccount savedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(savedBankAccount);
    }

    @Transactional
    public BankAccountResponseDTO updateBankAccount(Long id, BankAccountRequestDTO requestDTO) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));

        // Check if another bank account with this account number already exists
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
    public BankAccountResponseDTO changeStatus(Long id, AccountStatus status) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        bankAccount.setStatus(status);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(updatedBankAccount);
    }

    @Transactional
    public BankAccountResponseDTO setAsDefault(Long id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> BankAccountNotFoundException.byId(id));
        bankAccount.setIsDefault(true);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(updatedBankAccount);
    }

    @Transactional
    public BankAccountResponseDTO unsetAsDefault(Long id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
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
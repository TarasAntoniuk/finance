package com.tarasantoniuk.finance.banking.bank.service;

import com.tarasantoniuk.finance.banking.bank.dto.BankRequestDto;
import com.tarasantoniuk.finance.banking.bank.dto.BankResponseDto;
import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bank.exception.BankNotFoundException;
import com.tarasantoniuk.finance.banking.bank.exception.DuplicateBankException;
import com.tarasantoniuk.finance.banking.bank.mapper.BankMapper;
import com.tarasantoniuk.finance.banking.bank.repository.BankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BankService {

    private final BankRepository bankRepository;
    private final BankMapper bankMapper;

    public BankService(BankRepository bankRepository, BankMapper bankMapper) {
        this.bankRepository = bankRepository;
        this.bankMapper = bankMapper;
    }

    /**
     * Get all banks with optimized query (solves N+1 problem).
     * Uses JOIN FETCH to load country and counterparty in a single query.
     */
    public List<BankResponseDto> getAllBanks() {
        List<Bank> banks = bankRepository.findAllWithRelations();
        return bankMapper.toResponseList(banks);
    }

    /**
     * Get bank by ID with optimized query.
     */
    public BankResponseDto getBankById(Long id) {
        Bank bank = bankRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankNotFoundException.byId(id));
        return bankMapper.toResponse(bank);
    }

    /**
     * Get banks by country with optimized query.
     */
    public List<BankResponseDto> getBanksByCountry(Long countryId) {
        List<Bank> banks = bankRepository.findByCountryIdWithRelations(countryId);
        return bankMapper.toResponseList(banks);
    }

    /**
     * Get active banks with optimized query.
     */
    public List<BankResponseDto> getActiveBanks() {
        List<Bank> banks = bankRepository.findActiveWithRelations();
        return bankMapper.toResponseList(banks);
    }

    /**
     * Get bank by SWIFT code with optimized query.
     */
    public BankResponseDto getBankBySwiftCode(String swiftCode) {
        Bank bank = bankRepository.findBySwiftCodeWithRelations(swiftCode)
                .orElseThrow(() -> BankNotFoundException.bySwiftCode(swiftCode));
        return bankMapper.toResponse(bank);
    }

    @Transactional
    public BankResponseDto createBank(BankRequestDto requestDTO) {
        if (bankRepository.findBySwiftCodeWithRelations(requestDTO.getSwiftCode()).isPresent()) {
            throw DuplicateBankException.bySwiftCode(requestDTO.getSwiftCode());
        }

        Bank bank = bankMapper.toEntity(requestDTO);
        Bank savedBank = bankRepository.save(bank);
        return bankMapper.toResponse(savedBank);
    }

    @Transactional
    public BankResponseDto updateBank(Long id, BankRequestDto requestDTO) {
        Bank bank = bankRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankNotFoundException.byId(id));

        bankRepository.findBySwiftCodeWithRelations(requestDTO.getSwiftCode())
                .ifPresent(existingBank -> {
                    if (!existingBank.getId().equals(id)) {
                        throw DuplicateBankException.bySwiftCode(requestDTO.getSwiftCode());
                    }
                });

        bankMapper.updateEntity(requestDTO, bank);
        Bank updatedBank = bankRepository.save(bank);
        return bankMapper.toResponse(updatedBank);
    }

    @Transactional
    public BankResponseDto deactivateBank(Long id) {
        Bank bank = bankRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankNotFoundException.byId(id));
        bank.setIsActive(false);
        Bank updatedBank = bankRepository.save(bank);
        return bankMapper.toResponse(updatedBank);
    }

    @Transactional
    public BankResponseDto activateBank(Long id) {
        Bank bank = bankRepository.findByIdWithRelations(id)
                .orElseThrow(() -> BankNotFoundException.byId(id));
        bank.setIsActive(true);
        Bank updatedBank = bankRepository.save(bank);
        return bankMapper.toResponse(updatedBank);
    }

    @Transactional
    public void deleteBank(Long id) {
        if (!bankRepository.existsById(id)) {
            throw BankNotFoundException.byId(id);
        }
        bankRepository.deleteById(id);
    }
}
package com.tarasantoniuk.finance.bank.service;

import com.tarasantoniuk.finance.bank.dto.BankRequestDTO;
import com.tarasantoniuk.finance.bank.dto.BankResponseDTO;
import com.tarasantoniuk.finance.bank.entity.Bank;
import com.tarasantoniuk.finance.bank.exception.BankNotFoundException;
import com.tarasantoniuk.finance.bank.exception.DuplicateBankException;
import com.tarasantoniuk.finance.bank.mapper.BankMapper;
import com.tarasantoniuk.finance.bank.repository.BankRepository;
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

    public List<BankResponseDTO> getAllBanks() {
        List<Bank> banks = bankRepository.findAll();
        return bankMapper.toResponseList(banks);
    }

    public BankResponseDTO getBankById(Long id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> BankNotFoundException.byId(id));
        return bankMapper.toResponse(bank);
    }

    public List<BankResponseDTO> getBanksByCountry(Long countryId) {
        List<Bank> banks = bankRepository.findByCountryId(countryId);
        return bankMapper.toResponseList(banks);
    }

    public List<BankResponseDTO> getActiveBanks() {
        List<Bank> banks = bankRepository.findByIsActiveTrue();
        return bankMapper.toResponseList(banks);
    }

    public BankResponseDTO getBankBySwiftCode(String swiftCode) {
        Bank bank = bankRepository.findBySwiftCode(swiftCode)
                .orElseThrow(() -> BankNotFoundException.bySwiftCode(swiftCode));
        return bankMapper.toResponse(bank);
    }

    @Transactional
    public BankResponseDTO createBank(BankRequestDTO requestDTO) {
        // Check if bank with this SWIFT code already exists
        if (bankRepository.findBySwiftCode(requestDTO.getSwiftCode()).isPresent()) {
            throw DuplicateBankException.bySwiftCode(requestDTO.getSwiftCode());
        }

        Bank bank = bankMapper.toEntity(requestDTO);
        Bank savedBank = bankRepository.save(bank);
        return bankMapper.toResponse(savedBank);
    }

    @Transactional
    public BankResponseDTO updateBank(Long id, BankRequestDTO requestDTO) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> BankNotFoundException.byId(id));

        // Check if another bank with this SWIFT code already exists
        bankRepository.findBySwiftCode(requestDTO.getSwiftCode())
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
    public BankResponseDTO deactivateBank(Long id) {
        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> BankNotFoundException.byId(id));
        bank.setIsActive(false);
        Bank updatedBank = bankRepository.save(bank);
        return bankMapper.toResponse(updatedBank);
    }

    @Transactional
    public BankResponseDTO activateBank(Long id) {
        Bank bank = bankRepository.findById(id)
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
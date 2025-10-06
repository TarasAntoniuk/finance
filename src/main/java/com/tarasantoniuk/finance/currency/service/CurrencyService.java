package com.tarasantoniuk.finance.currency.service;

import com.tarasantoniuk.finance.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.currency.dto.CurrencyResponseDTO;
import com.tarasantoniuk.finance.currency.entity.Currency;
import com.tarasantoniuk.finance.currency.exception.CurrencyAlreadyExistsException;
import com.tarasantoniuk.finance.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    public CurrencyService(CurrencyRepository currencyRepository, CurrencyMapper currencyMapper) {
        this.currencyRepository = currencyRepository;
        this.currencyMapper = currencyMapper;
    }

    @Transactional(readOnly = true)
    public List<CurrencyResponseDTO> getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();
        return currencyMapper.toResponseDTOList(currencies);
    }

    @Transactional(readOnly = true)
    public List<CurrencyResponseDTO> getActiveCurrencies() {
        List<Currency> currencies = currencyRepository.findByIsActive(true);
        return currencyMapper.toResponseDTOList(currencies);
    }

    @Transactional(readOnly = true)
    public CurrencyResponseDTO getCurrencyById(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> CurrencyNotFoundException.byId(id));
        return currencyMapper.toResponseDTO(currency);
    }

    @Transactional(readOnly = true)
    public CurrencyResponseDTO getCurrencyByCode(String code) {
        Currency currency = currencyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> CurrencyNotFoundException.byCode(code));
        return currencyMapper.toResponseDTO(currency);
    }

    @Transactional(readOnly = true)
    public CurrencyResponseDTO getCurrencyByNumericCode(String numericCode) {
        Currency currency = currencyRepository.findByNumericCode(numericCode)
                .orElseThrow(() -> CurrencyNotFoundException.byNumericCode(numericCode));
        return currencyMapper.toResponseDTO(currency);
    }

    @Transactional(readOnly = true)
    public List<CurrencyResponseDTO> searchCurrenciesByName(String name) {
        List<Currency> currencies = currencyRepository.findByNameContainingIgnoreCase(name);
        return currencyMapper.toResponseDTOList(currencies);
    }

    @Transactional
    public CurrencyResponseDTO createCurrency(CurrencyRequestDTO requestDTO) {
        if (currencyRepository.existsByCode(requestDTO.getCode().toUpperCase())) {
            throw CurrencyAlreadyExistsException.byCode(requestDTO.getCode());
        }

        if (currencyRepository.existsByNumericCode(requestDTO.getNumericCode())) {
            throw CurrencyAlreadyExistsException.byNumericCode(requestDTO.getNumericCode());
        }

        Currency currency = currencyMapper.toEntity(requestDTO);
        currency.setCode(currency.getCode().toUpperCase());
        Currency savedCurrency = currencyRepository.save(currency);
        return currencyMapper.toResponseDTO(savedCurrency);
    }

    @Transactional
    public CurrencyResponseDTO updateCurrency(Long id, CurrencyRequestDTO requestDTO) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> CurrencyNotFoundException.byId(id));

        String upperCode = requestDTO.getCode().toUpperCase();
        if (!currency.getCode().equals(upperCode) && currencyRepository.existsByCode(upperCode)) {
            throw CurrencyAlreadyExistsException.byCode(requestDTO.getCode());
        }

        if (!currency.getNumericCode().equals(requestDTO.getNumericCode())
                && currencyRepository.existsByNumericCode(requestDTO.getNumericCode())) {
            throw CurrencyAlreadyExistsException.byNumericCode(requestDTO.getNumericCode());
        }

        currencyMapper.updateEntityFromDTO(requestDTO, currency);
        currency.setCode(currency.getCode().toUpperCase());
        Currency updatedCurrency = currencyRepository.save(currency);
        return currencyMapper.toResponseDTO(updatedCurrency);
    }

    @Transactional
    public void deleteCurrency(Long id) {
        if (!currencyRepository.existsById(id)) {
            throw CurrencyNotFoundException.byId(id);
        }
        currencyRepository.deleteById(id);
    }

    @Transactional
    public CurrencyResponseDTO deactivateCurrency(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> CurrencyNotFoundException.byId(id));
        currency.setIsActive(false);
        Currency updatedCurrency = currencyRepository.save(currency);
        return currencyMapper.toResponseDTO(updatedCurrency);
    }

    @Transactional
    public CurrencyResponseDTO activateCurrency(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> CurrencyNotFoundException.byId(id));
        currency.setIsActive(true);
        Currency updatedCurrency = currencyRepository.save(currency);
        return currencyMapper.toResponseDTO(updatedCurrency);
    }
}
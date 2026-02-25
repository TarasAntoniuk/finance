package com.tarasantoniuk.finance.core.currency.service;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyRequestDto;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyAlreadyExistsException;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.core.currency.mapper.CurrencyMapper;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import org.springframework.data.domain.PageRequest;
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
    public List<CurrencyResponseDto> getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();
        return currencyMapper.toResponseDTOList(currencies);
    }

    @Transactional(readOnly = true)
    public List<CurrencyResponseDto> getActiveCurrencies() {
        List<Currency> currencies = currencyRepository.findByIsActive(true);
        return currencyMapper.toResponseDTOList(currencies);
    }

    @Transactional(readOnly = true)
    public CurrencyResponseDto getCurrencyById(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> CurrencyNotFoundException.byId(id));
        return currencyMapper.toResponseDTO(currency);
    }

    @Transactional(readOnly = true)
    public CurrencyResponseDto getCurrencyByCode(String code) {
        Currency currency = currencyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> CurrencyNotFoundException.byCode(code));
        return currencyMapper.toResponseDTO(currency);
    }

    @Transactional(readOnly = true)
    public CurrencyResponseDto getCurrencyByNumericCode(String numericCode) {
        Currency currency = currencyRepository.findByNumericCode(numericCode)
                .orElseThrow(() -> CurrencyNotFoundException.byNumericCode(numericCode));
        return currencyMapper.toResponseDTO(currency);
    }

    private static final int MAX_SEARCH_RESULTS = 500;

    @Transactional(readOnly = true)
    public List<CurrencyResponseDto> searchCurrenciesByName(String name) {
        List<Currency> currencies = currencyRepository.findByNameContainingIgnoreCase(name, PageRequest.of(0, MAX_SEARCH_RESULTS));
        return currencyMapper.toResponseDTOList(currencies);
    }

    @Transactional
    public CurrencyResponseDto createCurrency(CurrencyRequestDto requestDTO) {
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
    public CurrencyResponseDto updateCurrency(Long id, CurrencyRequestDto requestDTO) {
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
    public CurrencyResponseDto deactivateCurrency(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> CurrencyNotFoundException.byId(id));
        currency.setIsActive(false);
        Currency updatedCurrency = currencyRepository.save(currency);
        return currencyMapper.toResponseDTO(updatedCurrency);
    }

    @Transactional
    public CurrencyResponseDto activateCurrency(Long id) {
        Currency currency = currencyRepository.findById(id)
                .orElseThrow(() -> CurrencyNotFoundException.byId(id));
        currency.setIsActive(true);
        Currency updatedCurrency = currencyRepository.save(currency);
        return currencyMapper.toResponseDTO(updatedCurrency);
    }
}
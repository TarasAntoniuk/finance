package com.tarasantoniuk.finance.externalexchangerate.service;

import com.tarasantoniuk.finance.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateRequestDTO;
import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateResponseDTO;
import com.tarasantoniuk.finance.externalexchangerate.entity.ExternalExchangeRate;
import com.tarasantoniuk.finance.externalexchangerate.exception.ExchangeRateAlreadyExistsException;
import com.tarasantoniuk.finance.externalexchangerate.exception.ExchangeRateNotFoundException;
import com.tarasantoniuk.finance.externalexchangerate.exception.InvalidExchangeRateException;
import com.tarasantoniuk.finance.externalexchangerate.mapper.ExternalExchangeRateMapper;
import com.tarasantoniuk.finance.externalexchangerate.repository.ExternalExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExternalExchangeRateService {

    private final ExternalExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final ExternalExchangeRateMapper exchangeRateMapper;

    public ExternalExchangeRateService(ExternalExchangeRateRepository exchangeRateRepository,
                                       CurrencyRepository currencyRepository,
                                       ExternalExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getAllExchangeRates() {
        List<ExternalExchangeRate> rates = exchangeRateRepository.findAll();
        return exchangeRateMapper.toResponseDTOList(rates);
    }

    @Transactional(readOnly = true)
    public ExternalExchangeRateResponseDTO getExchangeRateById(Long id) {
        ExternalExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> ExchangeRateNotFoundException.byId(id));
        return exchangeRateMapper.toResponseDTO(rate);
    }

    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getExchangeRatesByDate(LocalDate date) {
        List<ExternalExchangeRate> rates = exchangeRateRepository.findByExchangeDate(date);
        return exchangeRateMapper.toResponseDTOList(rates);
    }

    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getExchangeRatesByDateAndSource(LocalDate date, String source) {
        List<ExternalExchangeRate> rates = exchangeRateRepository.findByExchangeDateAndSource(date, source);
        return exchangeRateMapper.toResponseDTOList(rates);
    }

    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getExchangeRatesByCurrencyPair(Long currencyFromId, Long currencyToId) {
        List<ExternalExchangeRate> rates = exchangeRateRepository
                .findByCurrencyFromIdAndCurrencyToId(currencyFromId, currencyToId);
        return exchangeRateMapper.toResponseDTOList(rates);
    }

    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getExchangeRatesBySource(String source) {
        List<ExternalExchangeRate> rates = exchangeRateRepository.findBySource(source);
        return exchangeRateMapper.toResponseDTOList(rates);
    }

    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getExchangeRatesByDateRange(
            LocalDate startDate, LocalDate endDate) {
        List<ExternalExchangeRate> rates = exchangeRateRepository
                .findByExchangeDateBetween(startDate, endDate);
        return exchangeRateMapper.toResponseDTOList(rates);
    }

    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getExchangeRatesByDateRangeAndCurrencyPair(
            LocalDate startDate, LocalDate endDate, Long currencyFromId, Long currencyToId) {
        List<ExternalExchangeRate> rates = exchangeRateRepository
                .findByExchangeDateBetweenAndCurrencyFromIdAndCurrencyToId(
                        startDate, endDate, currencyFromId, currencyToId);
        return exchangeRateMapper.toResponseDTOList(rates);
    }

    @Transactional(readOnly = true)
    public ExternalExchangeRateResponseDTO getLatestRateForCurrencyPair(
            Long currencyFromId, Long currencyToId, LocalDate date) {
        List<ExternalExchangeRate> rates = exchangeRateRepository
                .findLatestRateBeforeDate(date, currencyFromId, currencyToId);

        if (rates.isEmpty()) {
            throw ExchangeRateNotFoundException.byDateAndCurrencyPair(date, currencyFromId, currencyToId);
        }

        return exchangeRateMapper.toResponseDTO(rates.get(0));
    }

    @Transactional
    public ExternalExchangeRateResponseDTO createExchangeRate(ExternalExchangeRateRequestDTO requestDTO) {
        if (!currencyRepository.existsById(requestDTO.getCurrencyFromId())) {
            throw CurrencyNotFoundException.byId(requestDTO.getCurrencyFromId());
        }

        if (!currencyRepository.existsById(requestDTO.getCurrencyToId())) {
            throw CurrencyNotFoundException.byId(requestDTO.getCurrencyToId());
        }

        if (requestDTO.getCurrencyFromId().equals(requestDTO.getCurrencyToId())) {
            throw InvalidExchangeRateException.sameCurrency();
        }

        if (exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                requestDTO.getExchangeDate(), requestDTO.getCurrencyFromId(),
                requestDTO.getCurrencyToId(), requestDTO.getSource())) {
            throw ExchangeRateAlreadyExistsException.forDateCurrencyAndSource(
                    requestDTO.getExchangeDate(), requestDTO.getCurrencyFromId(),
                    requestDTO.getCurrencyToId(), requestDTO.getSource());
        }

        ExternalExchangeRate rate = exchangeRateMapper.toEntity(requestDTO);
        ExternalExchangeRate savedRate = exchangeRateRepository.save(rate);
        return exchangeRateMapper.toResponseDTO(savedRate);
    }

    @Transactional
    public ExternalExchangeRateResponseDTO updateExchangeRate(Long id, ExternalExchangeRateRequestDTO requestDTO) {
        ExternalExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> ExchangeRateNotFoundException.byId(id));

        if (!currencyRepository.existsById(requestDTO.getCurrencyFromId())) {
            throw CurrencyNotFoundException.byId(requestDTO.getCurrencyFromId());
        }

        if (!currencyRepository.existsById(requestDTO.getCurrencyToId())) {
            throw CurrencyNotFoundException.byId(requestDTO.getCurrencyToId());
        }

        if (requestDTO.getCurrencyFromId().equals(requestDTO.getCurrencyToId())) {
            throw InvalidExchangeRateException.sameCurrency();
        }

        // Check if changing to existing combination
        if ((!rate.getExchangeDate().equals(requestDTO.getExchangeDate())
                || !rate.getCurrencyFrom().getId().equals(requestDTO.getCurrencyFromId())
                || !rate.getCurrencyTo().getId().equals(requestDTO.getCurrencyToId())
                || !rate.getSource().equals(requestDTO.getSource()))
                && exchangeRateRepository.existsByExchangeDateAndCurrencyFromIdAndCurrencyToIdAndSource(
                requestDTO.getExchangeDate(), requestDTO.getCurrencyFromId(),
                requestDTO.getCurrencyToId(), requestDTO.getSource())) {
            throw ExchangeRateAlreadyExistsException.forDateCurrencyAndSource(
                    requestDTO.getExchangeDate(), requestDTO.getCurrencyFromId(),
                    requestDTO.getCurrencyToId(), requestDTO.getSource());
        }

        exchangeRateMapper.updateEntityFromDTO(requestDTO, rate);
        ExternalExchangeRate updatedRate = exchangeRateRepository.save(rate);
        return exchangeRateMapper.toResponseDTO(updatedRate);
    }

    @Transactional
    public void deleteExchangeRate(Long id) {
        if (!exchangeRateRepository.existsById(id)) {
            throw ExchangeRateNotFoundException.byId(id);
        }
        exchangeRateRepository.deleteById(id);
    }

    @Transactional
    public ExternalExchangeRateResponseDTO deactivateExchangeRate(Long id) {
        ExternalExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> ExchangeRateNotFoundException.byId(id));
        rate.setIsActive(false);
        ExternalExchangeRate updatedRate = exchangeRateRepository.save(rate);
        return exchangeRateMapper.toResponseDTO(updatedRate);
    }

    @Transactional
    public ExternalExchangeRateResponseDTO activateExchangeRate(Long id) {
        ExternalExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> ExchangeRateNotFoundException.byId(id));
        rate.setIsActive(true);
        ExternalExchangeRate updatedRate = exchangeRateRepository.save(rate);
        return exchangeRateMapper.toResponseDTO(updatedRate);
    }

    /**
     * Calculate cross rate: if we have USD/EUR and EUR/UAH, calculate USD/UAH
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateCrossRate(Long currencyFromId, Long currencyToId,
                                         Long intermediateCurrencyId, LocalDate date) {
        List<ExternalExchangeRate> rates1 = exchangeRateRepository
                .findLatestRateBeforeDate(date, currencyFromId, intermediateCurrencyId);

        if (rates1.isEmpty()) {
            throw ExchangeRateNotFoundException.byDateAndCurrencyPair(date, currencyFromId, intermediateCurrencyId);
        }

        List<ExternalExchangeRate> rates2 = exchangeRateRepository
                .findLatestRateBeforeDate(date, intermediateCurrencyId, currencyToId);

        if (rates2.isEmpty()) {
            throw ExchangeRateNotFoundException.byDateAndCurrencyPair(date, intermediateCurrencyId, currencyToId);
        }

        ExternalExchangeRate rate1 = rates1.get(0);
        ExternalExchangeRate rate2 = rates2.get(0);

        return rate1.getRate().multiply(rate2.getRate()).setScale(6, RoundingMode.HALF_UP);
    }
}
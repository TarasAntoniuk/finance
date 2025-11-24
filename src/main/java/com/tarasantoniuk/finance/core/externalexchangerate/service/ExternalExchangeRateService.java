package com.tarasantoniuk.finance.core.externalexchangerate.service;

import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.currency.exception.CurrencyNotFoundException;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateRequestDTO;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateResponseDTO;
import com.tarasantoniuk.finance.core.externalexchangerate.entity.ExternalExchangeRate;
import com.tarasantoniuk.finance.core.externalexchangerate.exception.ExchangeRateAlreadyExistsException;
import com.tarasantoniuk.finance.core.externalexchangerate.exception.ExchangeRateNotFoundException;
import com.tarasantoniuk.finance.core.externalexchangerate.exception.InvalidExchangeRateException;
import com.tarasantoniuk.finance.core.externalexchangerate.mapper.ExternalExchangeRateMapper;
import com.tarasantoniuk.finance.core.externalexchangerate.repository.ExternalExchangeRateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    /**
     * Get all exchange rates with optimized query (solves N+1 problem).
     * Uses JOIN FETCH to load currencies in a single query.
     */
    @Transactional(readOnly = true)
    public PageResponse<ExternalExchangeRateResponseDTO> getAllExchangeRates(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Using optimized method with JOIN FETCH
        Page<ExternalExchangeRate> ratePage = exchangeRateRepository.findAllWithCurrencies(pageable);

        List<ExternalExchangeRateResponseDTO> dtos = ratePage.getContent()
                .stream()
                .map(exchangeRateMapper::toResponseDTO)
                .toList();

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(ratePage.getNumber())
                .totalPages(ratePage.getTotalPages())
                .pageSize(ratePage.getSize())
                .totalElements(ratePage.getTotalElements())
                .hasNext(ratePage.hasNext())
                .hasPrevious(ratePage.hasPrevious())
                .build();

        return PageResponse.<ExternalExchangeRateResponseDTO>builder()
                .content(dtos)
                .metadata(metadata)
                .build();
    }

    /**
     * Get exchange rate by ID with optimized query.
     */
    @Transactional(readOnly = true)
    public ExternalExchangeRateResponseDTO getExchangeRateById(Long id) {
        ExternalExchangeRate rate = exchangeRateRepository.findByIdWithCurrencies(id)
                .orElseThrow(() -> ExchangeRateNotFoundException.byId(id));
        return exchangeRateMapper.toResponseDTO(rate);
    }

    /**
     * Get exchange rates by date with optimized query.
     */
    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getExchangeRatesByDate(LocalDate date) {
        List<ExternalExchangeRate> rates = exchangeRateRepository.findByExchangeDateWithCurrencies(date);
        return exchangeRateMapper.toResponseDTOList(rates);
    }

    /**
     * Get exchange rates by date and source with optimized query.
     */
    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getExchangeRatesByDateAndSource(LocalDate date, String source) {
        List<ExternalExchangeRate> rates = exchangeRateRepository
                .findByExchangeDateAndSourceWithCurrencies(date, source);
        return exchangeRateMapper.toResponseDTOList(rates);
    }

    /**
     * Get exchange rates by date range with optimized query.
     */
    @Transactional(readOnly = true)
    public PageResponse<ExternalExchangeRateResponseDTO> getExchangeRatesByDateRange(
            LocalDate startDate, LocalDate endDate, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ExternalExchangeRate> ratePage = exchangeRateRepository
                .findByExchangeDateBetweenWithCurrencies(startDate, endDate, pageable);

        List<ExternalExchangeRateResponseDTO> dtos = ratePage.getContent()
                .stream()
                .map(exchangeRateMapper::toResponseDTO)
                .toList();

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(ratePage.getNumber())
                .totalPages(ratePage.getTotalPages())
                .pageSize(ratePage.getSize())
                .totalElements(ratePage.getTotalElements())
                .hasNext(ratePage.hasNext())
                .hasPrevious(ratePage.hasPrevious())
                .build();

        return PageResponse.<ExternalExchangeRateResponseDTO>builder()
                .content(dtos)
                .metadata(metadata)
                .build();
    }

    /**
     * Get exchange rates by currency pair with optimized query.
     */
    @Transactional(readOnly = true)
    public PageResponse<ExternalExchangeRateResponseDTO> getExchangeRatesByCurrencyPair(
            Long currencyFromId, Long currencyToId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ExternalExchangeRate> ratePage = exchangeRateRepository
                .findByCurrencyPairWithCurrencies(currencyFromId, currencyToId, pageable);

        List<ExternalExchangeRateResponseDTO> dtos = ratePage.getContent()
                .stream()
                .map(exchangeRateMapper::toResponseDTO)
                .toList();

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(ratePage.getNumber())
                .totalPages(ratePage.getTotalPages())
                .pageSize(ratePage.getSize())
                .totalElements(ratePage.getTotalElements())
                .hasNext(ratePage.hasNext())
                .hasPrevious(ratePage.hasPrevious())
                .build();

        return PageResponse.<ExternalExchangeRateResponseDTO>builder()
                .content(dtos)
                .metadata(metadata)
                .build();
    }

    /**
     * Get latest rates by date and currency from with optimized query.
     */
    @Transactional(readOnly = true)
    public List<ExternalExchangeRateResponseDTO> getLatestRatesByDateAndCurrencyFrom(
            LocalDate date, Long currencyFromId) {

        if (!currencyRepository.existsById(currencyFromId)) {
            throw CurrencyNotFoundException.byId(currencyFromId);
        }

        List<ExternalExchangeRate> rates = exchangeRateRepository
                .findLatestRatesByCurrencyFromWithCurrencies(date, currencyFromId);

        return exchangeRateMapper.toResponseDTOList(rates);
    }

    /**
     * Calculate cross rate with optimized queries.
     * Example: if we have USD/EUR and EUR/UAH, calculate USD/UAH
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateCrossRate(Long currencyFromId, Long currencyToId,
                                         Long intermediateCurrencyId, LocalDate date) {
        List<ExternalExchangeRate> rates1 = exchangeRateRepository
                .findLatestRateBeforeDateWithCurrencies(date, currencyFromId, intermediateCurrencyId);

        if (rates1.isEmpty()) {
            throw ExchangeRateNotFoundException.byDateAndCurrencyPair(date, currencyFromId, intermediateCurrencyId);
        }

        List<ExternalExchangeRate> rates2 = exchangeRateRepository
                .findLatestRateBeforeDateWithCurrencies(date, intermediateCurrencyId, currencyToId);

        if (rates2.isEmpty()) {
            throw ExchangeRateNotFoundException.byDateAndCurrencyPair(date, intermediateCurrencyId, currencyToId);
        }

        ExternalExchangeRate rate1 = rates1.getFirst();
        ExternalExchangeRate rate2 = rates2.getFirst();

        return rate1.getRate().multiply(rate2.getRate()).setScale(6, RoundingMode.HALF_UP);
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
        ExternalExchangeRate rate = exchangeRateRepository.findByIdWithCurrencies(id)
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
        ExternalExchangeRate rate = exchangeRateRepository.findByIdWithCurrencies(id)
                .orElseThrow(() -> ExchangeRateNotFoundException.byId(id));
        rate.setIsActive(false);
        ExternalExchangeRate updatedRate = exchangeRateRepository.save(rate);
        return exchangeRateMapper.toResponseDTO(updatedRate);
    }

    @Transactional
    public ExternalExchangeRateResponseDTO activateExchangeRate(Long id) {
        ExternalExchangeRate rate = exchangeRateRepository.findByIdWithCurrencies(id)
                .orElseThrow(() -> ExchangeRateNotFoundException.byId(id));
        rate.setIsActive(true);
        ExternalExchangeRate updatedRate = exchangeRateRepository.save(rate);
        return exchangeRateMapper.toResponseDTO(updatedRate);
    }
}
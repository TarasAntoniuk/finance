package com.tarasantoniuk.finance.core.counterparty.service.impl;

import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.exception.CounterpartyNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.exception.DuplicateCounterpartyException;
import com.tarasantoniuk.finance.core.counterparty.mapper.CounterpartyMapper;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.counterparty.service.CounterpartyService;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CounterpartyServiceImpl implements CounterpartyService {

    private final CounterpartyRepository counterpartyRepository;
    private final CountryRepository countryRepository;
    private final CounterpartyMapper counterpartyMapper;

    public CounterpartyServiceImpl(CounterpartyRepository counterpartyRepository,
                                   CountryRepository countryRepository,
                                   CounterpartyMapper counterpartyMapper) {
        this.counterpartyRepository = counterpartyRepository;
        this.countryRepository = countryRepository;
        this.counterpartyMapper = counterpartyMapper;
    }

    @Override
    public CounterpartyResponseDto create(CounterpartyRequestDto request) {
        validateUniqueCode(request.getCode());

        Counterparty counterparty = counterpartyMapper.toEntity(request);
        setCountryIfProvided(request.getCountryId(), counterparty);

        Counterparty saved = counterpartyRepository.save(counterparty);
        return counterpartyMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CounterpartyResponseDto getById(Long id) {
        Counterparty counterparty = findCounterpartyById(id);
        return counterpartyMapper.toResponse(counterparty);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CounterpartyResponseDto> getAll(int page, int size) {
        // Sort by name alphabetically (A-Z)
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "name", "id"));

        Page<Counterparty> counterpartyPage = counterpartyRepository.findAll(pageable);

        // Map entities to DTOs
        List<CounterpartyResponseDto> dtos = counterpartyPage.getContent()
                .stream()
                .map(counterpartyMapper::toResponse)
                .toList();

        // Build metadata
        PageMetadata metadata = PageMetadata.builder()
                .currentPage(counterpartyPage.getNumber())
                .totalPages(counterpartyPage.getTotalPages())
                .pageSize(counterpartyPage.getSize())
                .totalElements(counterpartyPage.getTotalElements())
                .hasNext(counterpartyPage.hasNext())
                .hasPrevious(counterpartyPage.hasPrevious())
                .build();

        return PageResponse.<CounterpartyResponseDto>builder()
                .content(dtos)
                .metadata(metadata)
                .build();
    }

    @Override
    public CounterpartyResponseDto update(Long id, CounterpartyRequestDto request) {
        Counterparty counterparty = findCounterpartyById(id);

        if (request.getCode() != null && !request.getCode().equals(counterparty.getCode())) {
            validateUniqueCode(request.getCode());
        }

        counterpartyMapper.updateEntity(request, counterparty);
        setCountryIfProvided(request.getCountryId(), counterparty);

        Counterparty updated = counterpartyRepository.save(counterparty);
        return counterpartyMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!counterpartyRepository.existsById(id)) {
            throw CounterpartyNotFoundException.byId(id);
        }
        counterpartyRepository.deleteById(id);
    }

    @Override
    public void activate(Long id) {
        Counterparty counterparty = findCounterpartyById(id);
        counterparty.setIsActive(true);
        counterpartyRepository.save(counterparty);
    }

    @Override
    public void deactivate(Long id) {
        Counterparty counterparty = findCounterpartyById(id);
        counterparty.setIsActive(false);
        counterpartyRepository.save(counterparty);
    }

    private Counterparty findCounterpartyById(Long id) {
        return counterpartyRepository.findById(id)
                .orElseThrow(() -> CounterpartyNotFoundException.byId(id));
    }

    private void validateUniqueCode(String code) {
        if (counterpartyRepository.existsByCode(code)) {
            throw DuplicateCounterpartyException.byCode(code);
        }
    }

    private void setCountryIfProvided(Long countryId, Counterparty counterparty) {
        if (countryId != null) {
            Country country = countryRepository.findById(countryId)
                    .orElseThrow(() -> new CountryNotFoundException(countryId));
            counterparty.setCountry(country);
        }
    }
}
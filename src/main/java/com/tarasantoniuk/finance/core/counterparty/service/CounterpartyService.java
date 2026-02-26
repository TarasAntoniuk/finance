package com.tarasantoniuk.finance.core.counterparty.service;

import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.exception.CounterpartyNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.exception.DuplicateCounterpartyException;
import com.tarasantoniuk.finance.core.counterparty.mapper.CounterpartyMapper;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CounterpartyService {

    private final CounterpartyRepository counterpartyRepository;
    private final CountryRepository countryRepository;
    private final CounterpartyMapper counterpartyMapper;

    public CounterpartyService(CounterpartyRepository counterpartyRepository,
                               CountryRepository countryRepository,
                               CounterpartyMapper counterpartyMapper) {
        this.counterpartyRepository = counterpartyRepository;
        this.countryRepository = countryRepository;
        this.counterpartyMapper = counterpartyMapper;
    }

    @Transactional
    public CounterpartyResponseDto create(CounterpartyRequestDto request) {
        validateUniqueCode(request.getCode());

        Counterparty counterparty = counterpartyMapper.toEntity(request);
        setCountryIfProvided(request.getCountryId(), counterparty);

        Counterparty saved = counterpartyRepository.save(counterparty);
        return counterpartyMapper.toResponse(saved);
    }

    public CounterpartyResponseDto getById(Long id) {
        Counterparty counterparty = findCounterpartyByIdWithCountry(id);
        return counterpartyMapper.toResponse(counterparty);
    }

    public PageResponse<CounterpartyResponseDto> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "name", "id"));

        Page<Counterparty> counterpartyPage = counterpartyRepository.findAllWithCountry(pageable);

        List<CounterpartyResponseDto> dtos = counterpartyPage.getContent()
                .stream()
                .map(counterpartyMapper::toResponse)
                .toList();

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

    @Transactional
    public CounterpartyResponseDto update(Long id, CounterpartyRequestDto request) {
        Counterparty counterparty = findCounterpartyByIdWithCountry(id);

        if (request.getCode() != null && !request.getCode().equals(counterparty.getCode())) {
            validateUniqueCode(request.getCode());
        }

        counterpartyMapper.updateEntity(request, counterparty);
        setCountryIfProvided(request.getCountryId(), counterparty);

        Counterparty updated = counterpartyRepository.save(counterparty);
        return counterpartyMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!counterpartyRepository.existsById(id)) {
            throw CounterpartyNotFoundException.byId(id);
        }
        counterpartyRepository.deleteById(id);
    }

    @Transactional
    public CounterpartyResponseDto activate(Long id) {
        Counterparty counterparty = findCounterpartyByIdWithCountry(id);
        counterparty.setIsActive(true);
        Counterparty updated = counterpartyRepository.save(counterparty);
        return counterpartyMapper.toResponse(updated);
    }

    @Transactional
    public CounterpartyResponseDto deactivate(Long id) {
        Counterparty counterparty = findCounterpartyByIdWithCountry(id);
        counterparty.setIsActive(false);
        Counterparty updated = counterpartyRepository.save(counterparty);
        return counterpartyMapper.toResponse(updated);
    }

    private Counterparty findCounterpartyByIdWithCountry(Long id) {
        return counterpartyRepository.findByIdWithCountry(id)
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

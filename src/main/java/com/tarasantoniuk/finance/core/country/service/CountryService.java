package com.tarasantoniuk.finance.core.country.service;

import com.tarasantoniuk.finance.core.country.dto.CountryRequestDto;
import com.tarasantoniuk.finance.core.country.dto.CountryResponseDto;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.country.exception.CountryAlreadyExistsException;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.core.country.mapper.CountryMapper;
import com.tarasantoniuk.finance.core.country.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    public CountryService(CountryRepository countryRepository, CountryMapper countryMapper) {
        this.countryRepository = countryRepository;
        this.countryMapper = countryMapper;
    }

    /**
     * Get all countries with optimized query (solves N+1 problem).
     * Uses JOIN FETCH to load currency in a single query.
     */
    public List<CountryResponseDto> getAllCountries() {
        List<Country> countries = countryRepository.findAllWithCurrency();
        return countryMapper.toResponseDTOList(countries);
    }

    /**
     * Get country by ID with optimized query.
     */
    public CountryResponseDto getCountryById(Long id) {
        Country country = countryRepository.findByIdWithCurrency(id)
                .orElseThrow(() -> CountryNotFoundException.byId(id));
        return countryMapper.toResponseDTO(country);
    }

    /**
     * Get country by ISO code with optimized query.
     */
    public CountryResponseDto getCountryByIsoCode(String isoCode) {
        Country country = countryRepository.findByIsoCodeWithCurrency(isoCode)
                .orElseThrow(() -> CountryNotFoundException.byIsoCode(isoCode));
        return countryMapper.toResponseDTO(country);
    }

    @Transactional
    public CountryResponseDto createCountry(CountryRequestDto requestDTO) {
        if (countryRepository.existsByIsoCode(requestDTO.getIsoCode())) {
            throw CountryAlreadyExistsException.byIsoCode(requestDTO.getIsoCode());
        }

        Country country = countryMapper.toEntity(requestDTO);
        Country savedCountry = countryRepository.save(country);
        return countryMapper.toResponseDTO(savedCountry);
    }

    @Transactional
    public CountryResponseDto updateCountry(Long id, CountryRequestDto requestDTO) {
        Country country = countryRepository.findByIdWithCurrency(id)
                .orElseThrow(() -> CountryNotFoundException.byId(id));

        if (!country.getIsoCode().equals(requestDTO.getIsoCode())
                && countryRepository.existsByIsoCode(requestDTO.getIsoCode())) {
            throw CountryAlreadyExistsException.byIsoCode(requestDTO.getIsoCode());
        }

        countryMapper.updateEntityFromDTO(requestDTO, country);
        Country updatedCountry = countryRepository.save(country);
        return countryMapper.toResponseDTO(updatedCountry);
    }

    @Transactional
    public void deleteCountry(Long id) {
        if (!countryRepository.existsById(id)) {
            throw CountryNotFoundException.byId(id);
        }
        countryRepository.deleteById(id);
    }
}
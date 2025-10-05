package com.tarasantoniuk.finance.country.service;




import com.tarasantoniuk.finance.country.dto.CountryRequestDTO;
import com.tarasantoniuk.finance.country.dto.CountryResponseDTO;
import com.tarasantoniuk.finance.country.entity.Country;
import com.tarasantoniuk.finance.country.mapper.CountryMapper;
import com.tarasantoniuk.finance.country.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    public CountryService(CountryRepository countryRepository, CountryMapper countryMapper) {
        this.countryRepository = countryRepository;
        this.countryMapper = countryMapper;
    }

    @Transactional(readOnly = true)
    public List<CountryResponseDTO> getAllCountries() {
        List<Country> countries = countryRepository.findAll();
        return countryMapper.toResponseDTOList(countries);
    }

    @Transactional(readOnly = true)
    public CountryResponseDTO getCountryById(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));
        return countryMapper.toResponseDTO(country);
    }

    @Transactional(readOnly = true)
    public CountryResponseDTO getCountryByIsoCode(String isoCode) {
        Country country = countryRepository.findByIsoCode(isoCode)
                .orElseThrow(() -> new RuntimeException("Country not found with ISO code: " + isoCode));
        return countryMapper.toResponseDTO(country);
    }

    @Transactional
    public CountryResponseDTO createCountry(CountryRequestDTO requestDTO) {
        if (countryRepository.existsByIsoCode(requestDTO.getIsoCode())) {
            throw new RuntimeException("Country with ISO code " + requestDTO.getIsoCode() + " already exists");
        }

        Country country = countryMapper.toEntity(requestDTO);
        Country savedCountry = countryRepository.save(country);
        return countryMapper.toResponseDTO(savedCountry);
    }

    @Transactional
    public CountryResponseDTO updateCountry(Long id, CountryRequestDTO requestDTO) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with id: " + id));

        if (!country.getIsoCode().equals(requestDTO.getIsoCode())
                && countryRepository.existsByIsoCode(requestDTO.getIsoCode())) {
            throw new RuntimeException("Country with ISO code " + requestDTO.getIsoCode() + " already exists");
        }

        countryMapper.updateEntityFromDTO(requestDTO, country);
        Country updatedCountry = countryRepository.save(country);
        return countryMapper.toResponseDTO(updatedCountry);
    }

    @Transactional
    public void deleteCountry(Long id) {
        if (!countryRepository.existsById(id)) {
            throw new RuntimeException("Country not found with id: " + id);
        }
        countryRepository.deleteById(id);
    }
}
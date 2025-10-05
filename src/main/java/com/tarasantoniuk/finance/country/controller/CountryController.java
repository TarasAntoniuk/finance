package com.tarasantoniuk.finance.country.controller;


import com.tarasantoniuk.finance.country.dto.CountryRequestDTO;
import com.tarasantoniuk.finance.country.dto.CountryResponseDTO;
import com.tarasantoniuk.finance.country.service.CountryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    public ResponseEntity<List<CountryResponseDTO>> getAllCountries() {
        List<CountryResponseDTO> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryResponseDTO> getCountryById(@PathVariable Long id) {
        CountryResponseDTO country = countryService.getCountryById(id);
        return ResponseEntity.ok(country);
    }

    @GetMapping("/iso/{isoCode}")
    public ResponseEntity<CountryResponseDTO> getCountryByIsoCode(@PathVariable String isoCode) {
        CountryResponseDTO country = countryService.getCountryByIsoCode(isoCode);
        return ResponseEntity.ok(country);
    }

    @PostMapping
    public ResponseEntity<CountryResponseDTO> createCountry(@Valid @RequestBody CountryRequestDTO requestDTO) {
        CountryResponseDTO country = countryService.createCountry(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(country);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CountryResponseDTO> updateCountry(
            @PathVariable Long id,
            @Valid @RequestBody CountryRequestDTO requestDTO) {
        CountryResponseDTO country = countryService.updateCountry(id, requestDTO);
        return ResponseEntity.ok(country);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable Long id) {
        countryService.deleteCountry(id);
        return ResponseEntity.noContent().build();
    }
}
package com.tarasantoniuk.finance.core.country.controller;

import com.tarasantoniuk.finance.core.country.dto.CountryRequestDto;
import com.tarasantoniuk.finance.core.country.dto.CountryResponseDto;
import com.tarasantoniuk.finance.core.country.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@Tag(name = "Core - Country", description = "Country management API")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    @Operation(summary = "Get all countries", description = "Retrieve a list of all countries")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<CountryResponseDto>> getAllCountries() {
        List<CountryResponseDto> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get country by ID", description = "Retrieve a country by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Country found"),
            @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<CountryResponseDto> getCountryById(
            @Parameter(description = "Country ID", required = true) @PathVariable Long id) {
        CountryResponseDto country = countryService.getCountryById(id);
        return ResponseEntity.ok(country);
    }

    @GetMapping("/iso/{isoCode}")
    @Operation(summary = "Get country by ISO code", description = "Retrieve a country by its ISO code (e.g., USA, GBR)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Country found"),
            @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<CountryResponseDto> getCountryByIsoCode(
            @Parameter(description = "ISO country code", required = true, example = "USA")
            @PathVariable String isoCode) {
        CountryResponseDto country = countryService.getCountryByIsoCode(isoCode);
        return ResponseEntity.ok(country);
    }

    @PostMapping
    @Operation(summary = "Create a new country", description = "Create a new country record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Country created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<CountryResponseDto> createCountry(
            @Parameter(description = "Country data", required = true)
            @Valid @RequestBody CountryRequestDto requestDTO) {
        CountryResponseDto country = countryService.createCountry(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(country);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a country", description = "Update an existing country record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Country updated successfully"),
            @ApiResponse(responseCode = "404", description = "Country not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<CountryResponseDto> updateCountry(
            @Parameter(description = "Country ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated country data", required = true)
            @Valid @RequestBody CountryRequestDto requestDTO) {
        CountryResponseDto country = countryService.updateCountry(id, requestDTO);
        return ResponseEntity.ok(country);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a country", description = "Delete a country by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Country deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<Void> deleteCountry(
            @Parameter(description = "Country ID", required = true) @PathVariable Long id) {
        countryService.deleteCountry(id);
        return ResponseEntity.noContent().build();
    }
}
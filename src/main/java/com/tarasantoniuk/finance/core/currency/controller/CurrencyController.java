package com.tarasantoniuk.finance.core.currency.controller;


import com.tarasantoniuk.finance.core.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDTO;
import com.tarasantoniuk.finance.core.currency.service.CurrencyService;
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
@RequestMapping("/api/currencies")
@Tag(name = "Core - Currency", description = "Currency management API")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    @Operation(summary = "Get all currencies", description = "Retrieve a list of all currencies")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<CurrencyResponseDTO>> getAllCurrencies() {
        List<CurrencyResponseDTO> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active currencies", description = "Retrieve only active currencies")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<CurrencyResponseDTO>> getActiveCurrencies() {
        List<CurrencyResponseDTO> currencies = currencyService.getActiveCurrencies();
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get currency by ID", description = "Retrieve a currency by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency found"),
            @ApiResponse(responseCode = "404", description = "Currency not found")
    })
    public ResponseEntity<CurrencyResponseDTO> getCurrencyById(
            @Parameter(description = "Currency ID", required = true) @PathVariable Long id) {
        CurrencyResponseDTO currency = currencyService.getCurrencyById(id);
        return ResponseEntity.ok(currency);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get currency by code", description = "Retrieve a currency by its ISO 4217 code (e.g., USD, EUR)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency found"),
            @ApiResponse(responseCode = "404", description = "Currency not found")
    })
    public ResponseEntity<CurrencyResponseDTO> getCurrencyByCode(
            @Parameter(description = "Currency code", required = true, example = "USD")
            @PathVariable String code) {
        CurrencyResponseDTO currency = currencyService.getCurrencyByCode(code);
        return ResponseEntity.ok(currency);
    }

    @GetMapping("/numeric/{numericCode}")
    @Operation(summary = "Get currency by numeric code", description = "Retrieve a currency by its ISO 4217 numeric code (e.g., 840)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency found"),
            @ApiResponse(responseCode = "404", description = "Currency not found")
    })
    public ResponseEntity<CurrencyResponseDTO> getCurrencyByNumericCode(
            @Parameter(description = "Numeric currency code", required = true, example = "840")
            @PathVariable String numericCode) {
        CurrencyResponseDTO currency = currencyService.getCurrencyByNumericCode(numericCode);
        return ResponseEntity.ok(currency);
    }

    @GetMapping("/search")
    @Operation(summary = "Search currencies by name", description = "Search for currencies by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<CurrencyResponseDTO>> searchCurrenciesByName(
            @Parameter(description = "Currency name", required = true, example = "Dollar")
            @RequestParam String name) {
        List<CurrencyResponseDTO> currencies = currencyService.searchCurrenciesByName(name);
        return ResponseEntity.ok(currencies);
    }

    @PostMapping
    @Operation(summary = "Create a new currency", description = "Create a new currency record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Currency created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<CurrencyResponseDTO> createCurrency(
            @Parameter(description = "Currency data", required = true)
            @Valid @RequestBody CurrencyRequestDTO requestDTO) {
        CurrencyResponseDTO currency = currencyService.createCurrency(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(currency);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a currency", description = "Update an existing currency record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency updated successfully"),
            @ApiResponse(responseCode = "404", description = "Currency not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<CurrencyResponseDTO> updateCurrency(
            @Parameter(description = "Currency ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated currency data", required = true)
            @Valid @RequestBody CurrencyRequestDTO requestDTO) {
        CurrencyResponseDTO currency = currencyService.updateCurrency(id, requestDTO);
        return ResponseEntity.ok(currency);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a currency", description = "Mark a currency as inactive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Currency not found")
    })
    public ResponseEntity<CurrencyResponseDTO> deactivateCurrency(
            @Parameter(description = "Currency ID", required = true) @PathVariable Long id) {
        CurrencyResponseDTO currency = currencyService.deactivateCurrency(id);
        return ResponseEntity.ok(currency);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a currency", description = "Mark a currency as active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency activated successfully"),
            @ApiResponse(responseCode = "404", description = "Currency not found")
    })
    public ResponseEntity<CurrencyResponseDTO> activateCurrency(
            @Parameter(description = "Currency ID", required = true) @PathVariable Long id) {
        CurrencyResponseDTO currency = currencyService.activateCurrency(id);
        return ResponseEntity.ok(currency);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a currency", description = "Delete a currency by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Currency deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Currency not found")
    })
    public ResponseEntity<Void> deleteCurrency(
            @Parameter(description = "Currency ID", required = true) @PathVariable Long id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }
}
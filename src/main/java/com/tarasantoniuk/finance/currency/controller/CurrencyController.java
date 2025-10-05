package com.tarasantoniuk.finance.currency.controller;

import com.tarasantoniuk.finance.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.currency.dto.CurrencyResponseDTO;
import com.tarasantoniuk.finance.currency.service.CurrencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public ResponseEntity<List<CurrencyResponseDTO>> getAllCurrencies() {
        List<CurrencyResponseDTO> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CurrencyResponseDTO>> getActiveCurrencies() {
        List<CurrencyResponseDTO> currencies = currencyService.getActiveCurrencies();
        return ResponseEntity.ok(currencies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyResponseDTO> getCurrencyById(@PathVariable Long id) {
        CurrencyResponseDTO currency = currencyService.getCurrencyById(id);
        return ResponseEntity.ok(currency);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CurrencyResponseDTO> getCurrencyByCode(@PathVariable String code) {
        CurrencyResponseDTO currency = currencyService.getCurrencyByCode(code);
        return ResponseEntity.ok(currency);
    }

    @GetMapping("/numeric/{numericCode}")
    public ResponseEntity<CurrencyResponseDTO> getCurrencyByNumericCode(@PathVariable String numericCode) {
        CurrencyResponseDTO currency = currencyService.getCurrencyByNumericCode(numericCode);
        return ResponseEntity.ok(currency);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CurrencyResponseDTO>> searchCurrenciesByName(@RequestParam String name) {
        List<CurrencyResponseDTO> currencies = currencyService.searchCurrenciesByName(name);
        return ResponseEntity.ok(currencies);
    }

    @PostMapping
    public ResponseEntity<CurrencyResponseDTO> createCurrency(@Valid @RequestBody CurrencyRequestDTO requestDTO) {
        CurrencyResponseDTO currency = currencyService.createCurrency(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(currency);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CurrencyResponseDTO> updateCurrency(
            @PathVariable Long id,
            @Valid @RequestBody CurrencyRequestDTO requestDTO) {
        CurrencyResponseDTO currency = currencyService.updateCurrency(id, requestDTO);
        return ResponseEntity.ok(currency);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CurrencyResponseDTO> deactivateCurrency(@PathVariable Long id) {
        CurrencyResponseDTO currency = currencyService.deactivateCurrency(id);
        return ResponseEntity.ok(currency);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<CurrencyResponseDTO> activateCurrency(@PathVariable Long id) {
        CurrencyResponseDTO currency = currencyService.activateCurrency(id);
        return ResponseEntity.ok(currency);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable Long id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }
}
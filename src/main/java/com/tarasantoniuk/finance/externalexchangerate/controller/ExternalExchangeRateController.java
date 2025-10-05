package com.tarasantoniuk.finance.externalexchangerate.controller;

import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateRequestDTO;
import com.tarasantoniuk.finance.externalexchangerate.dto.ExternalExchangeRateResponseDTO;
import com.tarasantoniuk.finance.externalexchangerate.service.ExternalExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExternalExchangeRateController {

    private final ExternalExchangeRateService exchangeRateService;

    public ExternalExchangeRateController(ExternalExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public ResponseEntity<List<ExternalExchangeRateResponseDTO>> getAllExchangeRates() {
        List<ExternalExchangeRateResponseDTO> rates = exchangeRateService.getAllExchangeRates();
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalExchangeRateResponseDTO> getExchangeRateById(@PathVariable Long id) {
        ExternalExchangeRateResponseDTO rate = exchangeRateService.getExchangeRateById(id);
        return ResponseEntity.ok(rate);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<ExternalExchangeRateResponseDTO>> getExchangeRatesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ExternalExchangeRateResponseDTO> rates = exchangeRateService.getExchangeRatesByDate(date);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/date/{date}/source/{source}")
    public ResponseEntity<List<ExternalExchangeRateResponseDTO>> getExchangeRatesByDateAndSource(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String source) {
        List<ExternalExchangeRateResponseDTO> rates = exchangeRateService
                .getExchangeRatesByDateAndSource(date, source);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/currency-pair")
    public ResponseEntity<List<ExternalExchangeRateResponseDTO>> getExchangeRatesByCurrencyPair(
            @RequestParam Long currencyFromId,
            @RequestParam Long currencyToId) {
        List<ExternalExchangeRateResponseDTO> rates = exchangeRateService
                .getExchangeRatesByCurrencyPair(currencyFromId, currencyToId);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<List<ExternalExchangeRateResponseDTO>> getExchangeRatesBySource(
            @PathVariable String source) {
        List<ExternalExchangeRateResponseDTO> rates = exchangeRateService.getExchangeRatesBySource(source);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ExternalExchangeRateResponseDTO>> getExchangeRatesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ExternalExchangeRateResponseDTO> rates = exchangeRateService
                .getExchangeRatesByDateRange(startDate, endDate);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/date-range/currency-pair")
    public ResponseEntity<List<ExternalExchangeRateResponseDTO>> getExchangeRatesByDateRangeAndCurrencyPair(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam Long currencyFromId,
            @RequestParam Long currencyToId) {
        List<ExternalExchangeRateResponseDTO> rates = exchangeRateService
                .getExchangeRatesByDateRangeAndCurrencyPair(startDate, endDate, currencyFromId, currencyToId);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/latest")
    public ResponseEntity<ExternalExchangeRateResponseDTO> getLatestRateForCurrencyPair(
            @RequestParam Long currencyFromId,
            @RequestParam Long currencyToId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ExternalExchangeRateResponseDTO rate = exchangeRateService
                .getLatestRateForCurrencyPair(currencyFromId, currencyToId, date);
        return ResponseEntity.ok(rate);
    }

    @GetMapping("/cross-rate")
    public ResponseEntity<BigDecimal> calculateCrossRate(
            @RequestParam Long currencyFromId,
            @RequestParam Long currencyToId,
            @RequestParam Long intermediateCurrencyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        BigDecimal crossRate = exchangeRateService
                .calculateCrossRate(currencyFromId, currencyToId, intermediateCurrencyId, date);
        return ResponseEntity.ok(crossRate);
    }

    @PostMapping
    public ResponseEntity<ExternalExchangeRateResponseDTO> createExchangeRate(
            @Valid @RequestBody ExternalExchangeRateRequestDTO requestDTO) {
        ExternalExchangeRateResponseDTO rate = exchangeRateService.createExchangeRate(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(rate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExternalExchangeRateResponseDTO> updateExchangeRate(
            @PathVariable Long id,
            @Valid @RequestBody ExternalExchangeRateRequestDTO requestDTO) {
        ExternalExchangeRateResponseDTO rate = exchangeRateService.updateExchangeRate(id, requestDTO);
        return ResponseEntity.ok(rate);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ExternalExchangeRateResponseDTO> deactivateExchangeRate(@PathVariable Long id) {
        ExternalExchangeRateResponseDTO rate = exchangeRateService.deactivateExchangeRate(id);
        return ResponseEntity.ok(rate);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ExternalExchangeRateResponseDTO> activateExchangeRate(@PathVariable Long id) {
        ExternalExchangeRateResponseDTO rate = exchangeRateService.activateExchangeRate(id);
        return ResponseEntity.ok(rate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExchangeRate(@PathVariable Long id) {
        exchangeRateService.deleteExchangeRate(id);
        return ResponseEntity.noContent().build();
    }
}
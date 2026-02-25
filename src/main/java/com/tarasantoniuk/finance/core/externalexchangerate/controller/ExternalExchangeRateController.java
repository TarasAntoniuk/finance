package com.tarasantoniuk.finance.core.externalexchangerate.controller;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateRequestDto;
import com.tarasantoniuk.finance.core.externalexchangerate.dto.ExternalExchangeRateResponseDto;
import com.tarasantoniuk.finance.core.externalexchangerate.service.ExternalExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Core - Exchange Rate", description = "External exchange rate management API")
public class ExternalExchangeRateController {

    private static final int MAX_PAGE_SIZE = 500;

    private final ExternalExchangeRateService exchangeRateService;

    public ExternalExchangeRateController(ExternalExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    @Operation(
            summary = "Get all exchange rates",
            description = """
                    Retrieve a paginated list of all exchange rates, sorted by date (newest first).
                    
                    Examples:
                    - GET /api/exchange-rates - first page with default settings
                    - GET /api/exchange-rates?page=1 - second page
                    - GET /api/exchange-rates?page=0&size=100 - first page with 100 items
                    """
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list")
    public ResponseEntity<PageResponse<ExternalExchangeRateResponseDto>> getAllExchangeRates(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (max 500)", example = "200")
            @RequestParam(defaultValue = "200") int size) {

        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        PageResponse<ExternalExchangeRateResponseDto> response =
                exchangeRateService.getAllExchangeRates(page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exchange rate by ID", description = "Retrieve an exchange rate by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange rate found"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found")
    })
    public ResponseEntity<ExternalExchangeRateResponseDto> getExchangeRateById(
            @Parameter(description = "Exchange rate ID", required = true) @PathVariable Long id) {
        ExternalExchangeRateResponseDto rate = exchangeRateService.getExchangeRateById(id);
        return ResponseEntity.ok(rate);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get exchange rates by date", description = "Retrieve all exchange rates for a specific date")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<ExternalExchangeRateResponseDto>> getExchangeRatesByDate(
            @Parameter(description = "Exchange date", required = true, example = "2024-01-15")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ExternalExchangeRateResponseDto> rates = exchangeRateService.getExchangeRatesByDate(date);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/date/{date}/source/{source}")
    @Operation(summary = "Get exchange rates by date and source",
            description = "Retrieve exchange rates for a specific date from a specific source")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<ExternalExchangeRateResponseDto>> getExchangeRatesByDateAndSource(
            @Parameter(description = "Exchange date", required = true, example = "2024-01-15")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Source name", required = true, example = "NBU")
            @PathVariable String source) {
        List<ExternalExchangeRateResponseDto> rates = exchangeRateService
                .getExchangeRatesByDateAndSource(date, source);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/date-range")
    @Operation(
            summary = "Get exchange rates by date range",
            description = """
                    Retrieve paginated exchange rates within a date range, sorted by date (newest first).
                    
                    Examples:
                    - GET /api/exchange-rates/date-range?startDate=2024-01-01&endDate=2024-12-31
                    - GET /api/exchange-rates/date-range?startDate=2024-01-01&endDate=2024-12-31&page=1&size=100
                    """
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list")
    public ResponseEntity<PageResponse<ExternalExchangeRateResponseDto>> getExchangeRatesByDateRange(
            @Parameter(description = "Start date", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (max 500)", example = "200")
            @RequestParam(defaultValue = "200") int size) {

        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        PageResponse<ExternalExchangeRateResponseDto> response =
                exchangeRateService.getExchangeRatesByDateRange(startDate, endDate, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/currency-pair")
    @Operation(
            summary = "Get exchange rates by currency pair",
            description = """
                    Retrieve paginated exchange rates for a specific currency pair, sorted by date (newest first).
                    
                    Examples:
                    - GET /api/exchange-rates/currency-pair?currencyFromId=1&currencyToId=2
                    - GET /api/exchange-rates/currency-pair?currencyFromId=1&currencyToId=2&page=1&size=100
                    """
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list")
    public ResponseEntity<PageResponse<ExternalExchangeRateResponseDto>> getExchangeRatesByCurrencyPair(
            @Parameter(description = "Currency From ID", required = true, example = "1")
            @RequestParam Long currencyFromId,
            @Parameter(description = "Currency To ID", required = true, example = "2")
            @RequestParam Long currencyToId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (max 500)", example = "200")
            @RequestParam(defaultValue = "200") int size) {

        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        PageResponse<ExternalExchangeRateResponseDto> response =
                exchangeRateService.getExchangeRatesByCurrencyPair(currencyFromId, currencyToId, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest/{date}")
    @Operation(summary = "Get latest exchange rates for date",
            description = "Retrieve all latest exchange rates from a base currency for a specific date")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<ExternalExchangeRateResponseDto>> getLatestRatesByDate(
            @Parameter(description = "Date", required = true, example = "2025-11-03")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Currency From ID", required = true, example = "2")
            @RequestParam Long currencyFromId) {
        List<ExternalExchangeRateResponseDto> rates = exchangeRateService
                .getLatestRatesByDateAndCurrencyFrom(date, currencyFromId);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/cross-rate")
    @Operation(summary = "Calculate cross exchange rate",
            description = "Calculate cross rate using intermediate currency (e.g., USD/UAH via USD/EUR and EUR/UAH)")
    @ApiResponse(responseCode = "200", description = "Successfully calculated cross rate")
    public ResponseEntity<BigDecimal> calculateCrossRate(
            @Parameter(description = "Currency From ID", required = true) @RequestParam Long currencyFromId,
            @Parameter(description = "Currency To ID", required = true) @RequestParam Long currencyToId,
            @Parameter(description = "Intermediate Currency ID", required = true) @RequestParam Long intermediateCurrencyId,
            @Parameter(description = "Date", required = true, example = "2024-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        BigDecimal crossRate = exchangeRateService
                .calculateCrossRate(currencyFromId, currencyToId, intermediateCurrencyId, date);
        return ResponseEntity.ok(crossRate);
    }

    @PostMapping
    @Operation(summary = "Create a new exchange rate", description = "Create a new exchange rate record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exchange rate created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ExternalExchangeRateResponseDto> createExchangeRate(
            @Parameter(description = "Exchange rate data", required = true)
            @Valid @RequestBody ExternalExchangeRateRequestDto requestDTO) {
        ExternalExchangeRateResponseDto rate = exchangeRateService.createExchangeRate(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(rate);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an exchange rate", description = "Update an existing exchange rate record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange rate updated successfully"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ExternalExchangeRateResponseDto> updateExchangeRate(
            @Parameter(description = "Exchange rate ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated exchange rate data", required = true)
            @Valid @RequestBody ExternalExchangeRateRequestDto requestDTO) {
        ExternalExchangeRateResponseDto rate = exchangeRateService.updateExchangeRate(id, requestDTO);
        return ResponseEntity.ok(rate);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an exchange rate", description = "Mark an exchange rate as inactive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange rate deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found")
    })
    public ResponseEntity<ExternalExchangeRateResponseDto> deactivateExchangeRate(
            @Parameter(description = "Exchange rate ID", required = true) @PathVariable Long id) {
        ExternalExchangeRateResponseDto rate = exchangeRateService.deactivateExchangeRate(id);
        return ResponseEntity.ok(rate);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate an exchange rate", description = "Mark an exchange rate as active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange rate activated successfully"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found")
    })
    public ResponseEntity<ExternalExchangeRateResponseDto> activateExchangeRate(
            @Parameter(description = "Exchange rate ID", required = true) @PathVariable Long id) {
        ExternalExchangeRateResponseDto rate = exchangeRateService.activateExchangeRate(id);
        return ResponseEntity.ok(rate);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an exchange rate", description = "Delete an exchange rate by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Exchange rate deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Exchange rate not found")
    })
    public ResponseEntity<Void> deleteExchangeRate(
            @Parameter(description = "Exchange rate ID", required = true) @PathVariable Long id) {
        exchangeRateService.deleteExchangeRate(id);
        return ResponseEntity.noContent().build();
    }
}
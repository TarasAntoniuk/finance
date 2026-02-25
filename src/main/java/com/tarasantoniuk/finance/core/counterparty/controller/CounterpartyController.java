package com.tarasantoniuk.finance.core.counterparty.controller;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.counterparty.service.CounterpartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counterparties")
@Tag(name = "Core - Counterparty", description = "Counterparty management API")
public class CounterpartyController {

    private static final int MAX_PAGE_SIZE = 500;

    private final CounterpartyService counterpartyService;

    public CounterpartyController(CounterpartyService counterpartyService) {
        this.counterpartyService = counterpartyService;
    }

    @PostMapping
    @Operation(summary = "Create counterparty")
    public ResponseEntity<CounterpartyResponseDto> create(@Valid @RequestBody CounterpartyRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(counterpartyService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get counterparty by ID")
    public ResponseEntity<CounterpartyResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(counterpartyService.getById(id));
    }

    @GetMapping
    @Operation(
            summary = "Get all counterparties",
            description = """
                    Retrieve a paginated list of all counterparties, sorted by name (A-Z).
                    
                    Examples:
                    - GET /api/counterparties - first page with default settings
                    - GET /api/counterparties?page=1 - second page
                    - GET /api/counterparties?page=0&size=50 - first page with 50 items
                    """
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list")
    public ResponseEntity<PageResponse<CounterpartyResponseDto>> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (max 500)", example = "50")
            @RequestParam(defaultValue = "50") int size) {

        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        PageResponse<CounterpartyResponseDto> response =
                counterpartyService.getAll(page, size);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update counterparty")
    public ResponseEntity<CounterpartyResponseDto> update(@PathVariable Long id,
                                                          @Valid @RequestBody CounterpartyRequestDto request) {
        return ResponseEntity.ok(counterpartyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete counterparty")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        counterpartyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate counterparty")
    public ResponseEntity<CounterpartyResponseDto> activate(@PathVariable Long id) {
        return ResponseEntity.ok(counterpartyService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate counterparty")
    public ResponseEntity<CounterpartyResponseDto> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(counterpartyService.deactivate(id));
    }
}
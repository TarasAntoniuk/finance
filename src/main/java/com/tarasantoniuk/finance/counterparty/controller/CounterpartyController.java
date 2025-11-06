package com.tarasantoniuk.finance.counterparty.controller;

import com.tarasantoniuk.finance.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.counterparty.service.CounterpartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counterparties")
@Tag(name = "Counterparty", description = "Counterparty management API")
public class CounterpartyController {

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
    @Operation(summary = "Get all counterparties")
    public ResponseEntity<List<CounterpartyResponseDto>> getAll() {
        return ResponseEntity.ok(counterpartyService.getAll());
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
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        counterpartyService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate counterparty")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        counterpartyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
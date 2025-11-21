package com.tarasantoniuk.finance.banking.bank.controller;

import com.tarasantoniuk.finance.banking.bank.dto.BankRequestDTO;
import com.tarasantoniuk.finance.banking.bank.dto.BankResponseDTO;
import com.tarasantoniuk.finance.banking.bank.service.BankService;
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
@RequestMapping("/api/banks")
@Tag(name = "Bank", description = "Bank management API")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping
    @Operation(summary = "Get all banks", description = "Retrieve a list of all banks")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<BankResponseDTO>> getAllBanks() {
        List<BankResponseDTO> banks = bankService.getAllBanks();
        return ResponseEntity.ok(banks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank by ID", description = "Retrieve a bank by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank found"),
            @ApiResponse(responseCode = "404", description = "Bank not found")
    })
    public ResponseEntity<BankResponseDTO> getBankById(
            @Parameter(description = "Bank ID", required = true) @PathVariable Long id) {
        BankResponseDTO bank = bankService.getBankById(id);
        return ResponseEntity.ok(bank);
    }

    @GetMapping("/country/{countryId}")
    @Operation(summary = "Get banks by country", description = "Retrieve all banks for a specific country")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<BankResponseDTO>> getBanksByCountry(
            @Parameter(description = "Country ID", required = true) @PathVariable Long countryId) {
        List<BankResponseDTO> banks = bankService.getBanksByCountry(countryId);
        return ResponseEntity.ok(banks);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active banks", description = "Retrieve all active banks")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<BankResponseDTO>> getActiveBanks() {
        List<BankResponseDTO> banks = bankService.getActiveBanks();
        return ResponseEntity.ok(banks);
    }

    @GetMapping("/swift/{swiftCode}")
    @Operation(summary = "Get bank by SWIFT code", description = "Retrieve a bank by its SWIFT/BIC code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank found"),
            @ApiResponse(responseCode = "404", description = "Bank not found")
    })
    public ResponseEntity<BankResponseDTO> getBankBySwiftCode(
            @Parameter(description = "SWIFT/BIC code", required = true) @PathVariable String swiftCode) {
        BankResponseDTO bank = bankService.getBankBySwiftCode(swiftCode);
        return ResponseEntity.ok(bank);
    }

    @PostMapping
    @Operation(summary = "Create a new bank", description = "Create a new bank record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bank created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BankResponseDTO> createBank(
            @Parameter(description = "Bank data", required = true)
            @Valid @RequestBody BankRequestDTO requestDTO) {
        BankResponseDTO bank = bankService.createBank(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(bank);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bank", description = "Update an existing bank record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank updated successfully"),
            @ApiResponse(responseCode = "404", description = "Bank not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BankResponseDTO> updateBank(
            @Parameter(description = "Bank ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated bank data", required = true)
            @Valid @RequestBody BankRequestDTO requestDTO) {
        BankResponseDTO bank = bankService.updateBank(id, requestDTO);
        return ResponseEntity.ok(bank);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a bank", description = "Mark a bank as inactive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Bank not found")
    })
    public ResponseEntity<BankResponseDTO> deactivateBank(
            @Parameter(description = "Bank ID", required = true) @PathVariable Long id) {
        BankResponseDTO bank = bankService.deactivateBank(id);
        return ResponseEntity.ok(bank);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a bank", description = "Mark a bank as active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank activated successfully"),
            @ApiResponse(responseCode = "404", description = "Bank not found")
    })
    public ResponseEntity<BankResponseDTO> activateBank(
            @Parameter(description = "Bank ID", required = true) @PathVariable Long id) {
        BankResponseDTO bank = bankService.activateBank(id);
        return ResponseEntity.ok(bank);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank", description = "Delete a bank by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bank deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Bank not found")
    })
    public ResponseEntity<Void> deleteBank(
            @Parameter(description = "Bank ID", required = true) @PathVariable Long id) {
        bankService.deleteBank(id);
        return ResponseEntity.noContent().build();
    }
}
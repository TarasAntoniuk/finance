package com.tarasantoniuk.finance.core.accountingpolicy.controller;

import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyRequestDTO;
import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyResponseDTO;
import com.tarasantoniuk.finance.core.accountingpolicy.service.AccountingPolicyService;
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
@RequestMapping("/api/accounting-policies")
@Tag(name = "Core - Accounting Policy", description = "Accounting policy management API")
public class AccountingPolicyController {

    private final AccountingPolicyService accountingPolicyService;

    public AccountingPolicyController(AccountingPolicyService accountingPolicyService) {
        this.accountingPolicyService = accountingPolicyService;
    }

    @GetMapping
    @Operation(summary = "Get all accounting policies", description = "Retrieve a list of all accounting policies")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAllAccountingPolicies() {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService.getAllAccountingPolicies();
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accounting policy by ID", description = "Retrieve an accounting policy by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy found"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found")
    })
    public ResponseEntity<AccountingPolicyResponseDTO> getAccountingPolicyById(
            @Parameter(description = "Accounting policy ID", required = true) @PathVariable Long id) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.getAccountingPolicyById(id);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/organization/{organizationId}/year/{year}")
    @Operation(summary = "Get accounting policy by organization and year",
            description = "Retrieve an accounting policy for specific organization and year")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy found"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found")
    })
    public ResponseEntity<AccountingPolicyResponseDTO> getAccountingPolicyByOrganizationAndYear(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long organizationId,
            @Parameter(description = "Year", required = true, example = "2024") @PathVariable Integer year) {
        AccountingPolicyResponseDTO policy = accountingPolicyService
                .getAccountingPolicyByOrganizationAndYear(organizationId, year);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get accounting policies by organization",
            description = "Retrieve all accounting policies for a specific organization")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAccountingPoliciesByOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long organizationId) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getAccountingPoliciesByOrganization(organizationId);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/organization/{organizationId}/active")
    @Operation(summary = "Get active accounting policies by organization",
            description = "Retrieve active accounting policies for a specific organization")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getActiveAccountingPoliciesByOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long organizationId) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getActiveAccountingPoliciesByOrganization(organizationId);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Get accounting policies by year",
            description = "Retrieve all accounting policies for a specific year")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAccountingPoliciesByYear(
            @Parameter(description = "Year", required = true, example = "2024") @PathVariable Integer year) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getAccountingPoliciesByYear(year);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/year-range")
    @Operation(summary = "Get accounting policies by year range",
            description = "Retrieve accounting policies within a year range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAccountingPoliciesByYearRange(
            @Parameter(description = "Start year", required = true, example = "2020") @RequestParam Integer startYear,
            @Parameter(description = "End year", required = true, example = "2024") @RequestParam Integer endYear) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getAccountingPoliciesByYearRange(startYear, endYear);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/currency/{currencyId}")
    @Operation(summary = "Get accounting policies by currency",
            description = "Retrieve all accounting policies using a specific currency")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAccountingPoliciesByCurrency(
            @Parameter(description = "Currency ID", required = true) @PathVariable Long currencyId) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getAccountingPoliciesByCurrency(currencyId);
        return ResponseEntity.ok(policies);
    }

    @PostMapping
    @Operation(summary = "Create a new accounting policy", description = "Create a new accounting policy record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Accounting policy created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<AccountingPolicyResponseDTO> createAccountingPolicy(
            @Parameter(description = "Accounting policy data", required = true)
            @Valid @RequestBody AccountingPolicyRequestDTO requestDTO) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.createAccountingPolicy(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(policy);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an accounting policy", description = "Update an existing accounting policy record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy updated successfully"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<AccountingPolicyResponseDTO> updateAccountingPolicy(
            @Parameter(description = "Accounting policy ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated accounting policy data", required = true)
            @Valid @RequestBody AccountingPolicyRequestDTO requestDTO) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.updateAccountingPolicy(id, requestDTO);
        return ResponseEntity.ok(policy);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an accounting policy", description = "Mark an accounting policy as inactive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found")
    })
    public ResponseEntity<AccountingPolicyResponseDTO> deactivateAccountingPolicy(
            @Parameter(description = "Accounting policy ID", required = true) @PathVariable Long id) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.deactivateAccountingPolicy(id);
        return ResponseEntity.ok(policy);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate an accounting policy", description = "Mark an accounting policy as active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy activated successfully"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found")
    })
    public ResponseEntity<AccountingPolicyResponseDTO> activateAccountingPolicy(
            @Parameter(description = "Accounting policy ID", required = true) @PathVariable Long id) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.activateAccountingPolicy(id);
        return ResponseEntity.ok(policy);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an accounting policy", description = "Delete an accounting policy by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Accounting policy deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found")
    })
    public ResponseEntity<Void> deleteAccountingPolicy(
            @Parameter(description = "Accounting policy ID", required = true) @PathVariable Long id) {
        accountingPolicyService.deleteAccountingPolicy(id);
        return ResponseEntity.noContent().build();
    }
}
package com.tarasantoniuk.finance.core.accountingpolicy.controller;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyRequestDto;
import com.tarasantoniuk.finance.core.accountingpolicy.dto.AccountingPolicyResponseDto;
import com.tarasantoniuk.finance.core.accountingpolicy.service.AccountingPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounting-policies")
@Validated
@Tag(name = "Core - Accounting Policy", description = "Accounting policy management API")
public class AccountingPolicyController {

    private static final int MAX_PAGE_SIZE = 500;

    private final AccountingPolicyService accountingPolicyService;

    public AccountingPolicyController(AccountingPolicyService accountingPolicyService) {
        this.accountingPolicyService = accountingPolicyService;
    }

    @GetMapping
    @Operation(summary = "Get all accounting policies", description = "Retrieve a paginated list of all accounting policies")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list")
    public ResponseEntity<PageResponse<AccountingPolicyResponseDto>> getAllAccountingPolicies(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page (max 500)", example = "50")
            @RequestParam(defaultValue = "50") @Min(1) int size) {
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        PageResponse<AccountingPolicyResponseDto> response = accountingPolicyService.getAllAccountingPolicies(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accounting policy by ID", description = "Retrieve an accounting policy by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy found"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found")
    })
    public ResponseEntity<AccountingPolicyResponseDto> getAccountingPolicyById(
            @Parameter(description = "Accounting policy ID", required = true) @PathVariable Long id) {
        AccountingPolicyResponseDto policy = accountingPolicyService.getAccountingPolicyById(id);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/organization/{organizationId}/year/{year}")
    @Operation(summary = "Get accounting policy by organization and year",
            description = "Retrieve an accounting policy for specific organization and year")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy found"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found")
    })
    public ResponseEntity<AccountingPolicyResponseDto> getAccountingPolicyByOrganizationAndYear(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long organizationId,
            @Parameter(description = "Year", required = true, example = "2024") @PathVariable Integer year) {
        AccountingPolicyResponseDto policy = accountingPolicyService
                .getAccountingPolicyByOrganizationAndYear(organizationId, year);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get accounting policies by organization",
            description = "Retrieve all accounting policies for a specific organization")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDto>> getAccountingPoliciesByOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long organizationId,
            @Parameter(description = "Maximum number of results (max 500)", example = "500")
            @RequestParam(defaultValue = "500") @Min(1) int limit) {
        if (limit > MAX_PAGE_SIZE) {
            limit = MAX_PAGE_SIZE;
        }
        List<AccountingPolicyResponseDto> policies = accountingPolicyService
                .getAccountingPoliciesByOrganization(organizationId, limit);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/organization/{organizationId}/active")
    @Operation(summary = "Get active accounting policies by organization",
            description = "Retrieve active accounting policies for a specific organization")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDto>> getActiveAccountingPoliciesByOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long organizationId,
            @Parameter(description = "Maximum number of results (max 500)", example = "500")
            @RequestParam(defaultValue = "500") @Min(1) int limit) {
        if (limit > MAX_PAGE_SIZE) {
            limit = MAX_PAGE_SIZE;
        }
        List<AccountingPolicyResponseDto> policies = accountingPolicyService
                .getActiveAccountingPoliciesByOrganization(organizationId, limit);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Get accounting policies by year",
            description = "Retrieve all accounting policies for a specific year")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDto>> getAccountingPoliciesByYear(
            @Parameter(description = "Year", required = true, example = "2024") @PathVariable Integer year,
            @Parameter(description = "Maximum number of results (max 500)", example = "500")
            @RequestParam(defaultValue = "500") @Min(1) int limit) {
        if (limit > MAX_PAGE_SIZE) {
            limit = MAX_PAGE_SIZE;
        }
        List<AccountingPolicyResponseDto> policies = accountingPolicyService
                .getAccountingPoliciesByYear(year, limit);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/year-range")
    @Operation(summary = "Get accounting policies by year range",
            description = "Retrieve accounting policies within a year range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDto>> getAccountingPoliciesByYearRange(
            @Parameter(description = "Start year", required = true, example = "2020") @RequestParam Integer startYear,
            @Parameter(description = "End year", required = true, example = "2024") @RequestParam Integer endYear,
            @Parameter(description = "Maximum number of results (max 500)", example = "500")
            @RequestParam(defaultValue = "500") @Min(1) int limit) {
        if (limit > MAX_PAGE_SIZE) {
            limit = MAX_PAGE_SIZE;
        }
        List<AccountingPolicyResponseDto> policies = accountingPolicyService
                .getAccountingPoliciesByYearRange(startYear, endYear, limit);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/currency/{currencyId}")
    @Operation(summary = "Get accounting policies by currency",
            description = "Retrieve all accounting policies using a specific currency")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<AccountingPolicyResponseDto>> getAccountingPoliciesByCurrency(
            @Parameter(description = "Currency ID", required = true) @PathVariable Long currencyId,
            @Parameter(description = "Maximum number of results (max 500)", example = "500")
            @RequestParam(defaultValue = "500") @Min(1) int limit) {
        if (limit > MAX_PAGE_SIZE) {
            limit = MAX_PAGE_SIZE;
        }
        List<AccountingPolicyResponseDto> policies = accountingPolicyService
                .getAccountingPoliciesByCurrency(currencyId, limit);
        return ResponseEntity.ok(policies);
    }

    @PostMapping
    @Operation(summary = "Create a new accounting policy", description = "Create a new accounting policy record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Accounting policy created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<AccountingPolicyResponseDto> createAccountingPolicy(
            @Parameter(description = "Accounting policy data", required = true)
            @Valid @RequestBody AccountingPolicyRequestDto requestDTO) {
        AccountingPolicyResponseDto policy = accountingPolicyService.createAccountingPolicy(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(policy);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an accounting policy", description = "Update an existing accounting policy record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy updated successfully"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<AccountingPolicyResponseDto> updateAccountingPolicy(
            @Parameter(description = "Accounting policy ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated accounting policy data", required = true)
            @Valid @RequestBody AccountingPolicyRequestDto requestDTO) {
        AccountingPolicyResponseDto policy = accountingPolicyService.updateAccountingPolicy(id, requestDTO);
        return ResponseEntity.ok(policy);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an accounting policy", description = "Mark an accounting policy as inactive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found")
    })
    public ResponseEntity<AccountingPolicyResponseDto> deactivateAccountingPolicy(
            @Parameter(description = "Accounting policy ID", required = true) @PathVariable Long id) {
        AccountingPolicyResponseDto policy = accountingPolicyService.deactivateAccountingPolicy(id);
        return ResponseEntity.ok(policy);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate an accounting policy", description = "Mark an accounting policy as active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounting policy activated successfully"),
            @ApiResponse(responseCode = "404", description = "Accounting policy not found")
    })
    public ResponseEntity<AccountingPolicyResponseDto> activateAccountingPolicy(
            @Parameter(description = "Accounting policy ID", required = true) @PathVariable Long id) {
        AccountingPolicyResponseDto policy = accountingPolicyService.activateAccountingPolicy(id);
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
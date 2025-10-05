package com.tarasantoniuk.finance.accountingpolicy.controller;

import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyRequestDTO;
import com.tarasantoniuk.finance.accountingpolicy.dto.AccountingPolicyResponseDTO;
import com.tarasantoniuk.finance.accountingpolicy.service.AccountingPolicyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounting-policies")
public class AccountingPolicyController {

    private final AccountingPolicyService accountingPolicyService;

    public AccountingPolicyController(AccountingPolicyService accountingPolicyService) {
        this.accountingPolicyService = accountingPolicyService;
    }

    @GetMapping
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAllAccountingPolicies() {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService.getAllAccountingPolicies();
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountingPolicyResponseDTO> getAccountingPolicyById(@PathVariable Long id) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.getAccountingPolicyById(id);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/organization/{organizationId}/year/{year}")
    public ResponseEntity<AccountingPolicyResponseDTO> getAccountingPolicyByOrganizationAndYear(
            @PathVariable Long organizationId,
            @PathVariable Integer year) {
        AccountingPolicyResponseDTO policy = accountingPolicyService
                .getAccountingPolicyByOrganizationAndYear(organizationId, year);
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAccountingPoliciesByOrganization(
            @PathVariable Long organizationId) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getAccountingPoliciesByOrganization(organizationId);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/organization/{organizationId}/active")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getActiveAccountingPoliciesByOrganization(
            @PathVariable Long organizationId) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getActiveAccountingPoliciesByOrganization(organizationId);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAccountingPoliciesByYear(
            @PathVariable Integer year) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getAccountingPoliciesByYear(year);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/year-range")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAccountingPoliciesByYearRange(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getAccountingPoliciesByYearRange(startYear, endYear);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/currency/{currencyId}")
    public ResponseEntity<List<AccountingPolicyResponseDTO>> getAccountingPoliciesByCurrency(
            @PathVariable Long currencyId) {
        List<AccountingPolicyResponseDTO> policies = accountingPolicyService
                .getAccountingPoliciesByCurrency(currencyId);
        return ResponseEntity.ok(policies);
    }

    @PostMapping
    public ResponseEntity<AccountingPolicyResponseDTO> createAccountingPolicy(
            @Valid @RequestBody AccountingPolicyRequestDTO requestDTO) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.createAccountingPolicy(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(policy);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountingPolicyResponseDTO> updateAccountingPolicy(
            @PathVariable Long id,
            @Valid @RequestBody AccountingPolicyRequestDTO requestDTO) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.updateAccountingPolicy(id, requestDTO);
        return ResponseEntity.ok(policy);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<AccountingPolicyResponseDTO> deactivateAccountingPolicy(@PathVariable Long id) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.deactivateAccountingPolicy(id);
        return ResponseEntity.ok(policy);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<AccountingPolicyResponseDTO> activateAccountingPolicy(@PathVariable Long id) {
        AccountingPolicyResponseDTO policy = accountingPolicyService.activateAccountingPolicy(id);
        return ResponseEntity.ok(policy);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccountingPolicy(@PathVariable Long id) {
        accountingPolicyService.deleteAccountingPolicy(id);
        return ResponseEntity.noContent().build();
    }
}
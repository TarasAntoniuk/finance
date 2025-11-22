package com.tarasantoniuk.finance.banking.bankaccount.controller;

import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountRequestDTO;
import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountResponseDTO;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import com.tarasantoniuk.finance.banking.bankaccount.service.BankAccountService;
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
@RequestMapping("/api/bank-accounts")
@Tag(name = "Banking - Bank Account", description = "Bank account management API")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping
    @Operation(summary = "Get all bank accounts", description = "Retrieve a list of all bank accounts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<BankAccountResponseDTO>> getAllBankAccounts() {
        List<BankAccountResponseDTO> bankAccounts = bankAccountService.getAllBankAccounts();
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank account by ID", description = "Retrieve a bank account by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank account found"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public ResponseEntity<BankAccountResponseDTO> getBankAccountById(
            @Parameter(description = "Bank account ID", required = true) @PathVariable Long id) {
        BankAccountResponseDTO bankAccount = bankAccountService.getBankAccountById(id);
        return ResponseEntity.ok(bankAccount);
    }

    @GetMapping("/account-number/{accountNumber}")
    @Operation(summary = "Get bank account by account number", description = "Retrieve a bank account by its account number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank account found"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public ResponseEntity<BankAccountResponseDTO> getBankAccountByAccountNumber(
            @Parameter(description = "Account number", required = true) @PathVariable String accountNumber) {
        BankAccountResponseDTO bankAccount = bankAccountService.getBankAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(bankAccount);
    }

    @GetMapping("/holder/{holderType}/{holderId}")
    @Operation(summary = "Get bank accounts by holder", description = "Retrieve all bank accounts for a specific holder")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<BankAccountResponseDTO>> getBankAccountsByHolder(
            @Parameter(description = "Holder type (ORGANIZATION or COUNTERPARTY)", required = true) @PathVariable AccountHolderType holderType,
            @Parameter(description = "Holder ID", required = true) @PathVariable Long holderId) {
        List<BankAccountResponseDTO> bankAccounts = bankAccountService.getBankAccountsByHolder(holderType, holderId);
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/bank/{bankId}")
    @Operation(summary = "Get bank accounts by bank", description = "Retrieve all bank accounts for a specific bank")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<BankAccountResponseDTO>> getBankAccountsByBank(
            @Parameter(description = "Bank ID", required = true) @PathVariable Long bankId) {
        List<BankAccountResponseDTO> bankAccounts = bankAccountService.getBankAccountsByBank(bankId);
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bank accounts by status", description = "Retrieve all bank accounts with a specific status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<BankAccountResponseDTO>> getBankAccountsByStatus(
            @Parameter(description = "Account status (ACTIVE, INACTIVE, or CLOSED)", required = true) @PathVariable AccountStatus status) {
        List<BankAccountResponseDTO> bankAccounts = bankAccountService.getBankAccountsByStatus(status);
        return ResponseEntity.ok(bankAccounts);
    }

    @GetMapping("/holder/{holderType}/{holderId}/default")
    @Operation(summary = "Get default bank accounts by holder", description = "Retrieve default bank accounts for a specific holder")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<BankAccountResponseDTO>> getDefaultBankAccountsByHolder(
            @Parameter(description = "Holder type (ORGANIZATION or COUNTERPARTY)", required = true) @PathVariable AccountHolderType holderType,
            @Parameter(description = "Holder ID", required = true) @PathVariable Long holderId) {
        List<BankAccountResponseDTO> bankAccounts = bankAccountService.getDefaultBankAccountsByHolder(holderType, holderId);
        return ResponseEntity.ok(bankAccounts);
    }

    @PostMapping
    @Operation(summary = "Create a new bank account", description = "Create a new bank account record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bank account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BankAccountResponseDTO> createBankAccount(
            @Parameter(description = "Bank account data", required = true)
            @Valid @RequestBody BankAccountRequestDTO requestDTO) {
        BankAccountResponseDTO bankAccount = bankAccountService.createBankAccount(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccount);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bank account", description = "Update an existing bank account record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank account updated successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BankAccountResponseDTO> updateBankAccount(
            @Parameter(description = "Bank account ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated bank account data", required = true)
            @Valid @RequestBody BankAccountRequestDTO requestDTO) {
        BankAccountResponseDTO bankAccount = bankAccountService.updateBankAccount(id, requestDTO);
        return ResponseEntity.ok(bankAccount);
    }

    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Change bank account status", description = "Change the status of a bank account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public ResponseEntity<BankAccountResponseDTO> changeStatus(
            @Parameter(description = "Bank account ID", required = true) @PathVariable Long id,
            @Parameter(description = "New status", required = true) @PathVariable AccountStatus status) {
        BankAccountResponseDTO bankAccount = bankAccountService.changeStatus(id, status);
        return ResponseEntity.ok(bankAccount);
    }

    @PatchMapping("/{id}/set-default")
    @Operation(summary = "Set as default account", description = "Mark a bank account as default")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account set as default successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public ResponseEntity<BankAccountResponseDTO> setAsDefault(
            @Parameter(description = "Bank account ID", required = true) @PathVariable Long id) {
        BankAccountResponseDTO bankAccount = bankAccountService.setAsDefault(id);
        return ResponseEntity.ok(bankAccount);
    }

    @PatchMapping("/{id}/unset-default")
    @Operation(summary = "Unset as default account", description = "Remove default flag from a bank account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Default flag removed successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public ResponseEntity<BankAccountResponseDTO> unsetAsDefault(
            @Parameter(description = "Bank account ID", required = true) @PathVariable Long id) {
        BankAccountResponseDTO bankAccount = bankAccountService.unsetAsDefault(id);
        return ResponseEntity.ok(bankAccount);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank account", description = "Delete a bank account by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bank account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    public ResponseEntity<Void> deleteBankAccount(
            @Parameter(description = "Bank account ID", required = true) @PathVariable Long id) {
        bankAccountService.deleteBankAccount(id);
        return ResponseEntity.noContent().build();
    }
}
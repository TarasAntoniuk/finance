package com.tarasantoniuk.finance.banking.bankpayment.controller;

import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentRequestDto;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentResponseDto;
import com.tarasantoniuk.finance.banking.bankpayment.service.BankPaymentService;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/bank-payments")
public class BankPaymentController {

    private final BankPaymentService bankPaymentService;

    public BankPaymentController(BankPaymentService bankPaymentService) {
        this.bankPaymentService = bankPaymentService;
    }

    /**
     * Create new bank payment
     */
    @PostMapping
    public ResponseEntity<BankPaymentResponseDto> createBankPayment(
            @Valid @RequestBody BankPaymentRequestDto requestDto) {
        BankPaymentResponseDto responseDto = bankPaymentService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Update existing bank payment
     */
    @PutMapping("/{id}")
    public ResponseEntity<BankPaymentResponseDto> updateBankPayment(
            @PathVariable Long id,
            @Valid @RequestBody BankPaymentRequestDto requestDto) {
        BankPaymentResponseDto responseDto = bankPaymentService.update(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Get bank payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BankPaymentResponseDto> getBankPaymentById(@PathVariable Long id) {
        BankPaymentResponseDto responseDto = bankPaymentService.findById(id);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Get all bank payments with pagination
     */
    @GetMapping
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getAllBankPayments(
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get bank payments by account ID
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getBankPaymentsByAccountId(
            @PathVariable Long accountId,
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findByAccountId(accountId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get bank payments by counterparty ID
     */
    @GetMapping("/counterparty/{counterpartyId}")
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getBankPaymentsByCounterpartyId(
            @PathVariable Long counterpartyId,
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findByCounterpartyId(counterpartyId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get bank payments by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getBankPaymentsByStatus(
            @PathVariable DocumentStatus status,
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get bank payments by date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getBankPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete bank payment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBankPayment(@PathVariable Long id) {
        bankPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Post bank payment - проведення документа
     */
    @PostMapping("/{id}/post")
    public ResponseEntity<BankPaymentResponseDto> post(@PathVariable Long id) {
        BankPaymentResponseDto responseDto = bankPaymentService.post(id);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Unpost bank payment - відміна проведення
     */
    @PostMapping("/{id}/unpost")
    public ResponseEntity<BankPaymentResponseDto> unpost(@PathVariable Long id) {
        BankPaymentResponseDto responseDto = bankPaymentService.unpost(id);
        return ResponseEntity.ok(responseDto);
    }
}
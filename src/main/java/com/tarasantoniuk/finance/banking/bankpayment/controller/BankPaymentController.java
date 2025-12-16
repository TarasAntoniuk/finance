package com.tarasantoniuk.finance.banking.bankpayment.controller;

import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentRequestDto;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentResponseDto;
import com.tarasantoniuk.finance.banking.bankpayment.service.BankPaymentService;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Banking - Bank Payments", description = "Bank payment management API")
public class BankPaymentController {

    private final BankPaymentService bankPaymentService;

    public BankPaymentController(BankPaymentService bankPaymentService) {
        this.bankPaymentService = bankPaymentService;
    }

    @PostMapping
    @Operation(summary = "Create new bank payment",
            description = "Creates a new bank payment document with DRAFT status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bank payment successfully created",
                    content = @Content(schema = @Schema(implementation = BankPaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Account or counterparty not found",
                    content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Bank payment data",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = BankPaymentRequestDto.class),
                    examples = @ExampleObject(
                            name = "Supplier Payment",
                            value = """
                                    {
                                      "documentDate": "2025-12-02",
                                      "paymentType": "SUPPLIER_PAYMENT",
                                      "amount": 5000.00,
                                      "accountId": 1,
                                      "counterpartyId": 7,
                                      "counterpartyBankAccountId": 6,
                                      "currencyId": 2,
                                      "organizationId": 1,
                                      "description": "Payment for consulting services",
                                      "paymentPurpose": "Invoice #SUP-2025-100, November services",
                                      "paymentReference": "SUP-2025-100",
                                      "outgoingDocumentNumber": "PAY-OUT-12345",
                                      "outgoingDocumentDate": "2025-12-01",
                                      "transactionDate": "2025-12-02",
                                      "valueDate": "2025-12-02",
                                      "externalTransactionId": "BANK-TXN-OUT-2025-001"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<BankPaymentResponseDto> createBankPayment(
            @Valid @RequestBody BankPaymentRequestDto requestDto) {
        BankPaymentResponseDto responseDto = bankPaymentService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update bank payment",
            description = "Updates existing bank payment. Only documents with DRAFT status can be edited")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank payment successfully updated",
                    content = @Content(schema = @Schema(implementation = BankPaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or document already posted",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bank payment not found",
                    content = @Content)
    })
    public ResponseEntity<BankPaymentResponseDto> updateBankPayment(
            @Parameter(description = "Bank payment ID") @PathVariable Long id,
            @Valid @RequestBody BankPaymentRequestDto requestDto) {
        BankPaymentResponseDto responseDto = bankPaymentService.update(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank payment by ID",
            description = "Returns detailed information about a bank payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank payment found",
                    content = @Content(schema = @Schema(implementation = BankPaymentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Bank payment not found",
                    content = @Content)
    })
    public ResponseEntity<BankPaymentResponseDto> getBankPaymentById(
            @Parameter(description = "Bank payment ID") @PathVariable Long id) {
        BankPaymentResponseDto responseDto = bankPaymentService.findById(id);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    @Operation(
            summary = "Get all bank payments",
            description = """
                    Returns a paginated list of all bank payments. Default sorting by document date (descending).
                    
                    **Sort examples:**
                    - `sort=documentDate,desc` - sort by document date descending
                    - `sort=amount,asc` - sort by amount ascending
                    - `sort=id,desc` - sort by ID descending
                    """
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of bank payments")
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getAllBankPayments(
            @ParameterObject
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get payments by account",
            description = "Returns a list of bank payments for the specified bank account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of payments"),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content)
    })
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getBankPaymentsByAccountId(
            @Parameter(description = "Bank account ID") @PathVariable Long accountId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findByAccountId(accountId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/counterparty/{counterpartyId}")
    @Operation(summary = "Get payments by counterparty",
            description = "Returns a list of bank payments for the specified counterparty")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of payments"),
            @ApiResponse(responseCode = "404", description = "Counterparty not found",
                    content = @Content)
    })
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getBankPaymentsByCounterpartyId(
            @Parameter(description = "Counterparty ID") @PathVariable Long counterpartyId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findByCounterpartyId(counterpartyId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status",
            description = "Returns a list of bank payments with the specified status (DRAFT or POSTED)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of payments")
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getBankPaymentsByStatus(
            @Parameter(description = "Document status", example = "DRAFT") @PathVariable DocumentStatus status,
            @ParameterObject
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get payments by date range",
            description = "Returns a list of bank payments for the specified date period")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of payments"),
            @ApiResponse(responseCode = "400", description = "Invalid date format or start date is after end date",
                    content = @Content)
    })
    public ResponseEntity<PageResponse<BankPaymentResponseDto>> getBankPaymentsByDateRange(
            @Parameter(description = "Start date (inclusive)", example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2025-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @ParameterObject
            @PageableDefault(size = 20, sort = "documentDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankPaymentResponseDto> response = bankPaymentService.findByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bank payment",
            description = "Deletes a bank payment. Only documents with DRAFT status can be deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bank payment successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Cannot delete posted document",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bank payment not found",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteBankPayment(
            @Parameter(description = "Bank payment ID") @PathVariable Long id) {
        bankPaymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/post")
    @Operation(summary = "Post bank payment",
            description = "Posts the bank payment (changes status from DRAFT to POSTED) and creates corresponding accounting entries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank payment successfully posted",
                    content = @Content(schema = @Schema(implementation = BankPaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Document already posted or cannot be posted",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bank payment not found",
                    content = @Content)
    })
    public ResponseEntity<BankPaymentResponseDto> post(
            @Parameter(description = "Bank payment ID") @PathVariable Long id) {
        BankPaymentResponseDto responseDto = bankPaymentService.post(id);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{id}/unpost")
    @Operation(summary = "Unpost bank payment",
            description = "Unposts the bank payment (changes status from POSTED to DRAFT) and removes corresponding accounting entries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank payment successfully unposted",
                    content = @Content(schema = @Schema(implementation = BankPaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Document not posted or cannot be unposted",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Bank payment not found",
                    content = @Content)
    })
    public ResponseEntity<BankPaymentResponseDto> unpost(
            @Parameter(description = "Bank payment ID") @PathVariable Long id) {
        BankPaymentResponseDto responseDto = bankPaymentService.unpost(id);
        return ResponseEntity.ok(responseDto);
    }
}
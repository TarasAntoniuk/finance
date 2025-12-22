package com.tarasantoniuk.finance.banking.bankreceipt.controller;

import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptRequestDto;
import com.tarasantoniuk.finance.banking.bankreceipt.dto.BankReceiptResponseDto;
import com.tarasantoniuk.finance.banking.bankreceipt.service.BankReceiptService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/bank-receipts")
@Tag(name = "Banking - Bank Receipts", description = "Operations for managing bank receipt documents")
public class BankReceiptController {

    private final BankReceiptService bankReceiptService;

    public BankReceiptController(BankReceiptService bankReceiptService) {
        this.bankReceiptService = bankReceiptService;
    }

    @PostMapping
    @Operation(
            summary = "Create new bank receipt",
            description = "Creates a new bank receipt document in DRAFT status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Bank receipt created successfully",
                    content = @Content(schema = @Schema(implementation = BankReceiptResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Related entity not found"),
            @ApiResponse(responseCode = "409", description = "Receipt with external transaction ID already exists")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Bank receipt data",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = BankReceiptRequestDto.class),
                    examples = @ExampleObject(
                            name = "Customer Payment",
                            value = """
                                    {
                                      "transactionDateTime": "2025-12-02T10:30:00",
                                      "receiptType": "CUSTOMER_PAYMENT",
                                      "amount": 10000.00,
                                      "bankCommission": 50.00,
                                      "accountId": 1,
                                      "counterpartyId": 7,
                                      "counterpartyBankAccountId": 6,
                                      "currencyId": 2,
                                      "organizationId": 1,
                                      "description": "Payment for software development services",
                                      "paymentPurpose": "Invoice #INV-2025-125, December services",
                                      "paymentReference": "INV-2025-125",
                                      "incomingDocumentNumber": "PAY-DEC-12345",
                                      "valueDate": "2025-12-02",
                                      "externalTransactionId": "BANK-TXN-2025-DEC-001"
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<BankReceiptResponseDto> create(
            @Valid @RequestBody BankReceiptRequestDto requestDto) {
        BankReceiptResponseDto response = bankReceiptService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update bank receipt",
            description = "Updates an existing bank receipt. Only DRAFT receipts can be modified."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank receipt updated successfully",
                    content = @Content(schema = @Schema(implementation = BankReceiptResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found"),
            @ApiResponse(responseCode = "409", description = "Cannot update receipt in current status")
    })
    public ResponseEntity<BankReceiptResponseDto> update(
            @Parameter(description = "Bank receipt ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody BankReceiptRequestDto requestDto) {
        BankReceiptResponseDto response = bankReceiptService.update(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get bank receipt by ID",
            description = "Retrieves a bank receipt by its ID with all related entities"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank receipt found",
                    content = @Content(
                            schema = @Schema(implementation = BankReceiptResponseDto.class),
                            examples = @ExampleObject(
                                    name = "Bank Receipt Example",
                                    value = """
                                            {
                                              "id": 1,
                                              "transactionDateTime": "2025-12-02T10:30:00",
                                              "receiptType": "CUSTOMER_PAYMENT",
                                              "amount": 10000.00,
                                              "bankCommission": 50.00,
                                              "status": "POSTED",
                                              "account": {
                                                "id": 1,
                                                "accountNumber": "ES1234567890123456789012",
                                                "accountName": "Main Business Account"
                                              },
                                              "counterparty": {
                                                "id": 7,
                                                "name": "ACME Corporation"
                                              },
                                              "currency": {
                                                "id": 2,
                                                "code": "EUR",
                                                "name": "Euro"
                                              },
                                              "organization": {
                                                "id": 1,
                                                "name": "My Company Ltd"
                                              },
                                              "description": "Payment for software development services",
                                              "paymentPurpose": "Invoice #INV-2025-125, December services",
                                              "paymentReference": "INV-2025-125",
                                              "createdAt": "2025-12-02T10:30:00",
                                              "updatedAt": "2025-12-02T10:35:00",
                                              "postedAt": "2025-12-02T10:35:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found")
    })
    public ResponseEntity<BankReceiptResponseDto> getById(
            @Parameter(description = "Bank receipt ID", required = true)
            @PathVariable Long id) {
        BankReceiptResponseDto response = bankReceiptService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Get all bank receipts",
            description = """
                    Retrieves all bank receipts with pagination and sorting.
                    
                    Sort examples:
                    - sort=transactionDateTime,desc (default)
                    - sort=amount,asc
                    - sort=id,desc
                    
                    Pagination examples:
                    - page=0&size=20 (default)
                    - page=2&size=50
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts retrieved successfully",
            content = @Content(
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(
                            name = "Paginated Bank Receipts",
                            value = """
                                    {
                                      "content": [
                                        {
                                          "id": 1,
                                          "transactionDateTime": "2025-12-02T10:30:00",
                                          "receiptType": "CUSTOMER_PAYMENT",
                                          "amount": 10000.00,
                                          "bankCommission": 50.00,
                                          "status": "POSTED",
                                          "account": {
                                            "id": 1,
                                            "accountNumber": "ES1234567890123456789012",
                                            "accountName": "Main Business Account"
                                          },
                                          "counterparty": {
                                            "id": 7,
                                            "name": "ACME Corporation"
                                          },
                                          "currency": {
                                            "id": 2,
                                            "code": "EUR",
                                            "name": "Euro"
                                          },
                                          "organization": {
                                            "id": 1,
                                            "name": "My Company Ltd"
                                          },
                                          "description": "Payment for software development services",
                                          "createdAt": "2025-12-02T10:30:00",
                                          "updatedAt": "2025-12-02T10:35:00",
                                          "postedAt": "2025-12-02T10:35:00"
                                        }
                                      ],
                                      "metadata": {
                                        "currentPage": 0,
                                        "pageSize": 20,
                                        "totalElements": 150,
                                        "totalPages": 8,
                                        "hasNext": true,
                                        "hasPrevious": false
                                      }
                                    }
                                    """
                    )
            )
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getAll(
            @Parameter(
                    description = "Pagination and sorting parameters",
                    examples = {
                            @ExampleObject(name = "Default", value = "page=0&size=20&sort=transactionDateTime,desc"),
                            @ExampleObject(name = "Custom", value = "page=1&size=50&sort=amount,asc")
                    }
            )
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(
            summary = "Get bank receipts by account",
            description = "Retrieves all bank receipts for a specific bank account with pagination"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts for account retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getByAccountId(
            @Parameter(description = "Bank account ID", required = true, example = "1")
            @PathVariable Long accountId,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findByAccountId(accountId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/counterparty/{counterpartyId}")
    @Operation(
            summary = "Get bank receipts by counterparty",
            description = "Retrieves all bank receipts from a specific counterparty with pagination"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts for counterparty retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getByCounterpartyId(
            @Parameter(description = "Counterparty ID", required = true, example = "7")
            @PathVariable Long counterpartyId,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findByCounterpartyId(counterpartyId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get bank receipts by status",
            description = "Retrieves all bank receipts with a specific status (DRAFT, POSTED, CANCELLED) with pagination"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts with specified status retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getByStatus(
            @Parameter(
                    description = "Document status",
                    required = true,
                    schema = @Schema(
                            allowableValues = {"DRAFT", "POSTED", "CANCELLED"},
                            example = "DRAFT"
                    )
            )
            @PathVariable DocumentStatus status,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @Operation(
            summary = "Get bank receipts by date range",
            description = """
                    Retrieves all bank receipts within a specified date range with pagination.
                    The range is inclusive for both start and end dates.
                    Date format: YYYY-MM-DD (ISO 8601)
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of bank receipts in date range retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
    )
    public ResponseEntity<PageResponse<BankReceiptResponseDto>> getByDateRange(
            @Parameter(
                    description = "Start date (inclusive)",
                    required = true,
                    example = "2025-10-01"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(
                    description = "End date (inclusive)",
                    required = true,
                    example = "2025-12-31"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20, sort = "transactionDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        // Convert dates to datetime range (start of startDate to end of endDate)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        PageResponse<BankReceiptResponseDto> response = bankReceiptService.findByDateTimeRange(startDateTime, endDateTime, pageable);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete bank receipt",
            description = "Deletes a bank receipt. Only DRAFT receipts can be deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bank receipt deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete receipt in current status (only DRAFT receipts can be deleted)")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Bank receipt ID", required = true, example = "1")
            @PathVariable Long id) {
        bankReceiptService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/post")
    @Operation(
            summary = "Post bank receipt",
            description = """
                    Posts the bank receipt (changes status from DRAFT to POSTED) and creates corresponding accounting entries.
                    This operation:
                    - Changes document status from DRAFT to POSTED
                    - Creates accounting entries (debiting bank account, crediting income/liability accounts)
                    - Updates bank account balance
                    - Records posting timestamp
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank receipt successfully posted",
                    content = @Content(schema = @Schema(implementation = BankReceiptResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Document already posted or cannot be posted"),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found"),
            @ApiResponse(responseCode = "409", description = "Business rule violation (e.g., insufficient balance, closed period)")
    })
    public ResponseEntity<BankReceiptResponseDto> post(
            @Parameter(description = "Bank receipt ID", required = true, example = "1")
            @PathVariable Long id) {
        BankReceiptResponseDto responseDto = bankReceiptService.post(id);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{id}/unpost")
    @Operation(
            summary = "Unpost bank receipt",
            description = """
                    Unposts the bank receipt (changes status from POSTED to DRAFT) and removes corresponding accounting entries.
                    This operation:
                    - Changes document status from POSTED to DRAFT
                    - Removes all related accounting entries
                    - Reverses bank account balance changes
                    - Clears posting timestamp
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bank receipt successfully unposted",
                    content = @Content(schema = @Schema(implementation = BankReceiptResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Document not posted or cannot be unposted"),
            @ApiResponse(responseCode = "404", description = "Bank receipt not found"),
            @ApiResponse(responseCode = "409", description = "Business rule violation (e.g., closed period, dependent documents exist)")
    })
    public ResponseEntity<BankReceiptResponseDto> unpost(
            @Parameter(description = "Bank receipt ID", required = true, example = "1")
            @PathVariable Long id) {
        BankReceiptResponseDto responseDto = bankReceiptService.unpost(id);
        return ResponseEntity.ok(responseDto);
    }
}
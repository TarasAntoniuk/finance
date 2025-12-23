package com.tarasantoniuk.finance.banking.bankpayment.dto;

import com.tarasantoniuk.finance.banking.bankaccount.dto.BankAccountResponseDto;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.currency.dto.CurrencyResponseDto;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO for bank payment responses
 */
@Schema(description = "Bank payment response with full details and related entities")
public class BankPaymentResponseDto extends BaseBankPaymentDto {

    @Schema(description = "Unique identifier of the bank payment", example = "1")
    private Long id;

    @Schema(description = "Bank account from which the payment is made")
    private BankAccountResponseDto account;

    @Schema(description = "Counterparty (payee) information")
    private CounterpartyResponseDto counterparty;

    @Schema(description = "Counterparty's bank account information (if available)")
    private BankAccountResponseDto counterpartyBankAccount;

    @Schema(description = "Currency information")
    private CurrencyResponseDto currency;

    @Schema(description = "Organization making the payment")
    private OrganizationResponseDto organization;

    @Schema(
            description = "Current status of the document",
            example = "POSTED",
            allowableValues = {"DRAFT", "POSTED", "CANCELLED"}
    )
    private DocumentStatus status;

    @Schema(description = "Timestamp when the document was posted", example = "2025-12-02T10:35:00")
    private LocalDateTime postedAt;

    @Schema(description = "Timestamp when the document was cancelled (if applicable)", example = "2025-12-02T15:20:00")
    private LocalDateTime cancelledAt;

    @Schema(description = "Timestamp when the record was created", example = "2025-12-02T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the record was last updated", example = "2025-12-02T10:35:00")
    private LocalDateTime updatedAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BankAccountResponseDto getAccount() {
        return account;
    }

    public void setAccount(BankAccountResponseDto account) {
        this.account = account;
    }

    public CounterpartyResponseDto getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartyResponseDto counterparty) {
        this.counterparty = counterparty;
    }

    public BankAccountResponseDto getCounterpartyBankAccount() {
        return counterpartyBankAccount;
    }

    public void setCounterpartyBankAccount(BankAccountResponseDto counterpartyBankAccount) {
        this.counterpartyBankAccount = counterpartyBankAccount;
    }

    public CurrencyResponseDto getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyResponseDto currency) {
        this.currency = currency;
    }

    public OrganizationResponseDto getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationResponseDto organization) {
        this.organization = organization;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = postedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
package com.tarasantoniuk.finance.banking.bankpayment.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccountbalance.service.BankAccountBalanceService;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentRequestDto;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentResponseDto;
import com.tarasantoniuk.finance.banking.bankpayment.dto.InvoiceAllocationDto;
import com.tarasantoniuk.finance.banking.bankpayment.enums.PaymentType;
import com.tarasantoniuk.finance.banking.bankpayment.repository.BankPaymentRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.service.BankReceiptService;
import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.settlement.dto.OpenItemResponse;
import com.tarasantoniuk.finance.settlement.exception.SettlementAllocationException;
import com.tarasantoniuk.finance.settlement.exception.SettlementConflictException;
import com.tarasantoniuk.finance.settlement.model.DocumentRef;
import com.tarasantoniuk.finance.settlement.model.SettlementDocumentTypes;
import com.tarasantoniuk.finance.settlement.model.SettlementKey;
import com.tarasantoniuk.finance.settlement.service.SettlementQueryService;
import com.tarasantoniuk.finance.settlement.service.SettlementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Supplier and contractor payments reduce what is owed in counterparty settlements. Runs without a
 * test-managed transaction, so a refused posting really rolls back the bank event as well.
 */
@DisplayName("Supplier payments in settlements")
class SupplierPaymentSettlementIntegrationTest extends BaseIntegrationTest {

    @Autowired private BankPaymentService paymentService;
    @Autowired private BankPaymentRepository paymentRepository;
    @Autowired private BankReceiptService receiptService;
    @Autowired private BankReceiptRepository receiptRepository;
    @Autowired private BankAccountBalanceService balanceService;
    @Autowired private SettlementService settlementService;
    @Autowired private SettlementQueryService settlementQueryService;
    @Autowired private TestDataFactoryCore factoryCore;
    @Autowired private TestDataFactoryBanking factoryBanking;
    @Autowired private TestDataCleanerCore cleanerCore;
    @Autowired private TestDataCleanerBanking cleanerBanking;

    private Organization organization;
    private Counterparty supplier;
    private Currency currency;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
        Country country = factoryCore.createUkraine();
        currency = factoryCore.createUAH();
        organization = factoryCore.createDefaultOrganization(country);
        supplier = factoryCore.createSupplierCounterparty();
        Bank bank = factoryBanking.createPrivatBank(country);
        account = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
        fundAccount("10000.00");
    }

    @AfterEach
    void tearDown() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
    }

    @Test
    @DisplayName("A supplier payment settles the oldest unpaid invoice")
    void supplierPaymentSettlesOldestInvoice() {
        invoice(1, "1000.00", 30);
        invoice(2, "500.00", 10);

        post(draft(PaymentType.SUPPLIER_PAYMENT, "700.00"));

        assertThat(openItems()).extracting(OpenItemResponse::documentId, item -> item.openAmount().toPlainString())
                .containsExactly(org.assertj.core.groups.Tuple.tuple(1L, "800.0000"));
    }

    @Test
    @DisplayName("Explicit allocations settle exactly the listed invoices and are returned with the payment")
    void explicitAllocationIsStoredAndApplied() {
        invoice(1, "1000.00", 10);
        invoice(2, "500.00", 30);

        BankPaymentResponseDto created = draft(PaymentType.CONTRACTOR_PAYMENT, "300.00",
                new InvoiceAllocationDto(2L, new BigDecimal("300.00")));
        post(created);

        assertThat(paymentService.findById(created.getId()).getAllocations())
                .containsExactly(new InvoiceAllocationDto(2L, new BigDecimal("300.00")));
        assertThat(openItems()).extracting(OpenItemResponse::documentId, item -> item.openAmount().toPlainString())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, "1000.0000"),
                        org.assertj.core.groups.Tuple.tuple(2L, "200.0000"));
    }

    @Test
    @DisplayName("A payment of another type leaves settlements untouched")
    void otherPaymentTypeIsIgnored() {
        invoice(1, "1000.00", 10);

        post(draft(PaymentType.RENT, "700.00"));

        assertThat(openItems()).singleElement()
                .satisfies(item -> assertThat(item.openAmount()).isEqualByComparingTo("1000.00"));
    }

    @Test
    @DisplayName("Unposting a payment reopens the invoice")
    void unpostReopensInvoice() {
        invoice(1, "1000.00", 10);
        BankPaymentResponseDto payment = draft(PaymentType.SUPPLIER_PAYMENT, "1000.00");
        post(payment);
        assertThat(openItems()).isEmpty();

        paymentService.unpost(payment.getId());

        assertThat(openItems()).singleElement()
                .satisfies(item -> assertThat(item.openAmount()).isEqualByComparingTo("1000.00"));
    }

    @Test
    @DisplayName("A prepayment used by a later invoice blocks unposting the payment, and nothing changes")
    void usedPrepaymentBlocksUnpost() {
        BankPaymentResponseDto payment = draft(PaymentType.SUPPLIER_PAYMENT, "500.00");
        post(payment);
        invoice(1, "300.00", 10);
        BigDecimal balanceBefore = bankBalance();

        assertThatThrownBy(() -> paymentService.unpost(payment.getId()))
                .isInstanceOf(SettlementConflictException.class);

        assertThat(paymentRepository.findById(payment.getId()).orElseThrow().getStatus()).isEqualTo(DocumentStatus.POSTED);
        assertThat(bankBalance()).isEqualByComparingTo(balanceBefore);
    }

    @Test
    @DisplayName("An allocation above the unpaid amount refuses the posting, and the bank balance is unchanged")
    void invalidAllocationRollsBackThePosting() {
        invoice(1, "100.00", 10);
        BankPaymentResponseDto payment = draft(PaymentType.SUPPLIER_PAYMENT, "300.00",
                new InvoiceAllocationDto(1L, new BigDecimal("300.00")));
        BigDecimal balanceBefore = bankBalance();

        assertThatThrownBy(() -> paymentService.post(payment.getId()))
                .isInstanceOf(SettlementAllocationException.class);

        assertThat(paymentRepository.findById(payment.getId()).orElseThrow().getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(bankBalance()).isEqualByComparingTo(balanceBefore);
        assertThat(openItems()).singleElement()
                .satisfies(item -> assertThat(item.openAmount()).isEqualByComparingTo("100.00"));
    }

    @Test
    @DisplayName("Allocations are validated when the draft is saved")
    void allocationsAreValidatedOnSave() {
        assertThatThrownBy(() -> draft(PaymentType.SALARY, "100.00", new InvoiceAllocationDto(1L, BigDecimal.TEN)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("only allowed on supplier and contractor payments");
        assertThatThrownBy(() -> draft(PaymentType.SUPPLIER_PAYMENT, "100.00",
                new InvoiceAllocationDto(1L, BigDecimal.TEN), new InvoiceAllocationDto(1L, BigDecimal.ONE)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("only once per payment");
        assertThatThrownBy(() -> draft(PaymentType.SUPPLIER_PAYMENT, "100.00",
                new InvoiceAllocationDto(1L, new BigDecimal("80.00")), new InvoiceAllocationDto(2L, new BigDecimal("30.00"))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("exceed the payment amount");
    }

    private BankPaymentResponseDto draft(PaymentType type, String amount, InvoiceAllocationDto... allocations) {
        BankPaymentRequestDto request = new BankPaymentRequestDto();
        request.setAccountId(account.getId());
        request.setCounterpartyId(supplier.getId());
        request.setCurrencyId(currency.getId());
        request.setOrganizationId(organization.getId());
        request.setTransactionDateTime(LocalDateTime.now().minusMinutes(1));
        request.setPaymentType(type);
        request.setAmount(new BigDecimal(amount));
        request.setDescription("Payment");
        request.setAllocations(List.of(allocations));
        return paymentService.create(request);
    }

    private void post(BankPaymentResponseDto payment) {
        paymentService.post(payment.getId());
    }

    private void invoice(long id, String amount, int dueInDays) {
        settlementService.postInvoice(new SettlementKey(organization.getId(), supplier.getId(), currency.getId()),
                new DocumentRef(SettlementDocumentTypes.PURCHASE_INVOICE, id), LocalDateTime.now().minusDays(1),
                LocalDate.now().plusDays(dueInDays), new BigDecimal(amount));
    }

    private void fundAccount(String amount) {
        BankReceipt receipt = factoryBanking.createBankReceipt(account, supplier, currency, organization, new BigDecimal(amount));
        receipt.setTransactionDateTime(LocalDateTime.now().minusDays(2));
        receiptRepository.save(receipt);
        receiptService.post(receipt.getId());
    }

    private BigDecimal bankBalance() {
        return balanceService.calculateBalance(account.getId(), LocalDateTime.now().plusDays(1));
    }

    private List<OpenItemResponse> openItems() {
        return settlementQueryService.getOpenItems(supplier.getId(), null);
    }
}

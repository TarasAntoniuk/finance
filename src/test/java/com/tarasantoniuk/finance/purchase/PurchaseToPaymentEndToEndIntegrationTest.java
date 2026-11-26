package com.tarasantoniuk.finance.purchase;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankpayment.dto.BankPaymentRequestDto;
import com.tarasantoniuk.finance.banking.bankpayment.dto.InvoiceAllocationDto;
import com.tarasantoniuk.finance.banking.bankpayment.enums.PaymentType;
import com.tarasantoniuk.finance.banking.bankpayment.service.BankPaymentService;
import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.banking.bankreceipt.repository.BankReceiptRepository;
import com.tarasantoniuk.finance.banking.bankreceipt.service.BankReceiptService;
import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.item.entity.Item;
import com.tarasantoniuk.finance.core.item.enums.ItemType;
import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import com.tarasantoniuk.finance.core.item.repository.ItemRepository;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceLineRequest;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceRequest;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.service.PurchaseInvoiceService;
import com.tarasantoniuk.finance.settlement.dto.CounterpartyPayable;
import com.tarasantoniuk.finance.settlement.dto.OpenItemResponse;
import com.tarasantoniuk.finance.settlement.exception.SettlementConflictException;
import com.tarasantoniuk.finance.settlement.service.SettlementQueryService;
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
 * The whole purchase loop on real documents: two supplier invoices, a payment allocated to the
 * newer one, the debt per invoice, and the reversal rules between them.
 */
@DisplayName("Purchase invoice to supplier payment, end to end")
class PurchaseToPaymentEndToEndIntegrationTest extends BaseIntegrationTest {

    @Autowired private PurchaseInvoiceService invoiceService;
    @Autowired private BankPaymentService paymentService;
    @Autowired private BankReceiptService receiptService;
    @Autowired private BankReceiptRepository receiptRepository;
    @Autowired private SettlementQueryService settlementQueryService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private TestDataFactoryCore factoryCore;
    @Autowired private TestDataFactoryBanking factoryBanking;
    @Autowired private TestDataCleanerCore cleanerCore;
    @Autowired private TestDataCleanerBanking cleanerBanking;

    private Organization organization;
    private Counterparty supplier;
    private Currency currency;
    private BankAccount account;
    private Item consulting;

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
        consulting = new Item();
        consulting.setCode("CONS-H");
        consulting.setName("Consulting");
        consulting.setType(ItemType.SERVICE);
        consulting.setUnitCode("HUR");
        consulting.setVatCategory(VatCategory.S);
        consulting.setVatRate(new BigDecimal("20.00"));
        itemRepository.save(consulting);

        BankReceipt funding = factoryBanking.createBankReceipt(account, supplier, currency, organization, new BigDecimal("10000.00"));
        funding.setTransactionDateTime(LocalDateTime.now().minusDays(5));
        receiptRepository.save(funding);
        receiptService.post(funding.getId());
    }

    @AfterEach
    void tearDown() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
    }

    @Test
    void invoicePaymentAndReversalRules() {
        long olderInvoice = postedInvoice("INV-1", "10", 10);   // 1200.00, due in 10 days
        long newerInvoice = postedInvoice("INV-2", "5", 30);    // 600.00, due in 30 days

        long payment = paymentService.create(paymentRequest("600.00",
                new InvoiceAllocationDto(newerInvoice, new BigDecimal("600.00")))).getId();
        paymentService.post(payment);

        assertThat(openItems()).singleElement().satisfies(item -> {
            assertThat(item.documentId()).isEqualTo(olderInvoice);
            assertThat(item.openAmount()).isEqualByComparingTo("1200.00");
        });
        CounterpartyPayable payable = settlementQueryService.getPayables(null).getFirst();
        assertThat(payable.balance()).isEqualByComparingTo("1200.00");

        assertThatThrownBy(() -> invoiceService.unpost(newerInvoice)).isInstanceOf(SettlementConflictException.class);

        paymentService.unpost(payment);
        invoiceService.unpost(newerInvoice);

        assertThat(openItems()).extracting(OpenItemResponse::documentId).containsExactly(olderInvoice);
    }

    private long postedInvoice(String number, String hours, int dueInDays) {
        long id = invoiceService.create(new PurchaseInvoiceRequest(organization.getId(), supplier.getId(), number,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(dueInDays), null, currency.getId(), null, null,
                List.of(new PurchaseInvoiceLineRequest(consulting.getId(), null, new BigDecimal(hours), null,
                        new BigDecimal("100.00"), null, null)))).id();
        invoiceService.post(id);
        return id;
    }

    private BankPaymentRequestDto paymentRequest(String amount, InvoiceAllocationDto... allocations) {
        BankPaymentRequestDto request = new BankPaymentRequestDto();
        request.setAccountId(account.getId());
        request.setCounterpartyId(supplier.getId());
        request.setCurrencyId(currency.getId());
        request.setOrganizationId(organization.getId());
        request.setTransactionDateTime(LocalDateTime.now().minusMinutes(1));
        request.setPaymentType(PaymentType.SUPPLIER_PAYMENT);
        request.setAmount(new BigDecimal(amount));
        request.setDescription("Payment for INV-2");
        request.setAllocations(List.of(allocations));
        return request;
    }

    private List<OpenItemResponse> openItems() {
        return settlementQueryService.getOpenItems(supplier.getId(), null);
    }
}

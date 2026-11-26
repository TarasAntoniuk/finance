package com.tarasantoniuk.finance.settlement.service;

import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.settlement.dto.CounterpartyPayable;
import com.tarasantoniuk.finance.settlement.dto.OpenItemResponse;
import com.tarasantoniuk.finance.settlement.exception.SettlementAllocationException;
import com.tarasantoniuk.finance.settlement.exception.SettlementConflictException;
import com.tarasantoniuk.finance.settlement.model.DocumentRef;
import com.tarasantoniuk.finance.settlement.model.PaymentAllocation;
import com.tarasantoniuk.finance.settlement.model.SettlementKey;
import com.tarasantoniuk.finance.settlement.repository.SettlementMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@DisplayName("Counterparty settlements")
class SettlementServiceIntegrationTest extends BaseIntegrationTest {

    private static final String INVOICE = "PurchaseInvoice";
    private static final String PAYMENT = "BankPayment";

    @Autowired private SettlementService settlementService;
    @Autowired private SettlementQueryService queryService;
    @Autowired private SettlementMovementRepository movementRepository;
    @Autowired private TestDataFactoryCore factoryCore;
    @Autowired private TestDataCleanerCore cleanerCore;

    private SettlementKey key;
    private Counterparty supplier;
    private Organization organization;
    private Currency currency;

    @BeforeEach
    void setUp() {
        cleanerCore.cleanAll();
        Country country = factoryCore.createUkraine();
        currency = factoryCore.createUAH();
        organization = factoryCore.createDefaultOrganization(country);
        supplier = factoryCore.createSupplierCounterparty();
        key = new SettlementKey(organization.getId(), supplier.getId(), currency.getId());
    }

    @Nested
    @DisplayName("Invoices and payments")
    class Posting {

        @Test
        @DisplayName("An invoice opens a debt")
        void invoiceOpensDebt() {
            invoice(1, "1000.00", 30);

            assertThat(balance()).isEqualByComparingTo("1000.00");
            assertThat(openItems()).singleElement()
                    .satisfies(item -> assertThat(item.openAmount()).isEqualByComparingTo("1000.00"));
        }

        @Test
        @DisplayName("A payment settles invoices oldest due date first")
        void paymentSettlesOldestDueDateFirst() {
            invoice(1, "300.00", 10);
            invoice(2, "500.00", 5);

            payment(1, "600.00");

            assertThat(openItems()).singleElement().satisfies(item -> {
                assertThat(item.documentId()).isEqualTo(1L);
                assertThat(item.openAmount()).isEqualByComparingTo("200.00");
            });
            assertThat(balance()).isEqualByComparingTo("200.00");
        }

        @Test
        @DisplayName("A payment larger than the debt leaves a prepayment")
        void overpaymentBecomesPrepayment() {
            invoice(1, "300.00", 10);

            payment(1, "500.00");

            assertThat(openItems()).singleElement().satisfies(item -> {
                assertThat(item.documentType()).isEqualTo(PAYMENT);
                assertThat(item.openAmount()).isEqualByComparingTo("-200.00");
            });
            assertThat(balance()).isEqualByComparingTo("-200.00");
        }

        @Test
        @DisplayName("An invoice consumes earlier prepayments")
        void invoiceConsumesPrepayment() {
            payment(1, "500.00");

            invoice(1, "300.00", 10);

            assertThat(openItems()).singleElement().satisfies(item -> {
                assertThat(item.documentType()).isEqualTo(PAYMENT);
                assertThat(item.openAmount()).isEqualByComparingTo("-200.00");
            });
        }

        @Test
        @DisplayName("Explicit allocations settle exactly the listed invoices")
        void explicitAllocationSettlesListedInvoice() {
            invoice(1, "300.00", 5);
            invoice(2, "500.00", 10);

            payment(1, "400.00", new PaymentAllocation(ref(INVOICE, 2), new BigDecimal("400.00")));

            assertThat(openItems()).extracting(OpenItemResponse::documentId, OpenItemResponse::openAmount)
                    .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .containsExactly(
                            org.assertj.core.groups.Tuple.tuple(1L, new BigDecimal("300.00")),
                            org.assertj.core.groups.Tuple.tuple(2L, new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("A payment that covers the oldest invoice leaves later invoices untouched")
        void paymentStopsAtCoveredInvoices() {
            invoice(1, "300.00", 5);
            invoice(2, "500.00", 10);
            invoice(3, "700.00", 15);

            payment(1, "300.00");

            assertThat(openItems()).extracting(OpenItemResponse::documentId).containsExactly(2L, 3L);
        }

        @Test
        @DisplayName("An invoice smaller than the prepayments uses only the oldest ones")
        void invoiceUsesOldestPrepaymentsOnly() {
            payment(1, "300.00");
            payment(2, "300.00");

            invoice(1, "300.00", 10);

            assertThat(openItems()).singleElement()
                    .satisfies(item -> assertThat(item.documentId()).isEqualTo(2L));
        }

        @Test
        @DisplayName("An overdue invoice is reported as overdue")
        void overdueInvoiceIsReported() {
            invoice(1, "300.00", -1);
            invoice(2, "500.00", 10);

            CounterpartyPayable payable = queryService.getPayables(null).getFirst();

            assertThat(payable.overdue()).isEqualByComparingTo("300.00");
            assertThat(payable.openItems()).isEqualTo(2L);
            assertThat(openItems().getFirst().overdue()).isTrue();
        }
    }

    @Nested
    @DisplayName("Explicit allocation errors")
    class AllocationErrors {

        @Test
        void allocationAboveUnpaidAmountIsRejected() {
            invoice(1, "300.00", 5);

            assertThatThrownBy(() -> payment(1, "400.00",
                    new PaymentAllocation(ref(INVOICE, 1), new BigDecimal("400.00"))))
                    .isInstanceOf(SettlementAllocationException.class)
                    .hasMessageContaining("exceeds the unpaid");
        }

        @Test
        void allocationsAbovePaymentAreRejected() {
            invoice(1, "300.00", 5);
            invoice(2, "300.00", 5);

            assertThatThrownBy(() -> payment(1, "400.00",
                    new PaymentAllocation(ref(INVOICE, 1), new BigDecimal("300.00")),
                    new PaymentAllocation(ref(INVOICE, 2), new BigDecimal("200.00"))))
                    .isInstanceOf(SettlementAllocationException.class)
                    .hasMessageContaining("exceed the payment");
        }

        @Test
        void allocationToAnotherSuppliersInvoiceIsRejected() {
            Counterparty other = factoryCore.createCounterparty("Other Supplier", "SUP-OTHER",
                    Counterparty.CounterpartyType.SUPPLIER);
            SettlementKey otherKey = new SettlementKey(organization.getId(), other.getId(), currency.getId());
            settlementService.postInvoice(otherKey, ref(INVOICE, 9), LocalDateTime.now(), LocalDate.now(), new BigDecimal("300.00"));

            assertThatThrownBy(() -> payment(1, "100.00",
                    new PaymentAllocation(ref(INVOICE, 9), new BigDecimal("100.00"))))
                    .isInstanceOf(SettlementAllocationException.class)
                    .hasMessageContaining("another organization, counterparty or currency");
        }

        @Test
        void nonPositiveAmountsAreRejected() {
            assertThatThrownBy(() -> invoice(1, "0.00", 5))
                    .isInstanceOf(SettlementAllocationException.class)
                    .hasMessageContaining("Invoice amount must be positive");
            assertThatThrownBy(() -> payment(1, "-5.00"))
                    .isInstanceOf(SettlementAllocationException.class)
                    .hasMessageContaining("Payment amount must be positive");
            invoice(2, "100.00", 5);
            assertThatThrownBy(() -> payment(2, "50.00",
                    new PaymentAllocation(ref(INVOICE, 2), BigDecimal.ZERO)))
                    .isInstanceOf(SettlementAllocationException.class)
                    .hasMessageContaining("Allocation amount must be positive");
        }

        @Test
        void unknownCounterpartyIsRejected() {
            SettlementKey unknown = new SettlementKey(organization.getId(), 999_999L, currency.getId());

            assertThatThrownBy(() -> settlementService.postInvoice(unknown, ref(INVOICE, 1), LocalDateTime.now(),
                    LocalDate.now(), BigDecimal.TEN))
                    .isInstanceOf(com.tarasantoniuk.finance.common.exception.ResourceNotFoundException.class);
        }

        @Test
        void allocationToUnknownInvoiceIsRejected() {
            assertThatThrownBy(() -> payment(1, "100.00",
                    new PaymentAllocation(ref(INVOICE, 404), new BigDecimal("100.00"))))
                    .isInstanceOf(SettlementAllocationException.class)
                    .hasMessageContaining("has no unpaid amount");
        }
    }

    @Nested
    @DisplayName("Reversal")
    class Reversal {

        @Test
        @DisplayName("Reversing a payment reopens the invoices it settled")
        void reversingPaymentReopensInvoices() {
            invoice(1, "1000.00", 10);
            payment(1, "1200.00");

            settlementService.reverse(ref(PAYMENT, 1));

            assertThat(openItems()).singleElement().satisfies(item -> {
                assertThat(item.documentType()).isEqualTo(INVOICE);
                assertThat(item.openAmount()).isEqualByComparingTo("1000.00");
            });
        }

        @Test
        @DisplayName("An invoice with payments cannot be reversed until the payments are")
        void invoiceWithPaymentsCannotBeReversed() {
            invoice(1, "1000.00", 10);
            payment(1, "600.00");

            assertThatThrownBy(() -> settlementService.reverse(ref(INVOICE, 1)))
                    .isInstanceOf(SettlementConflictException.class);

            settlementService.reverse(ref(PAYMENT, 1));
            settlementService.reverse(ref(INVOICE, 1));

            assertThat(openItems()).isEmpty();
            assertThat(queryService.getPayables(null)).isEmpty();
        }

        @Test
        @DisplayName("A prepayment used by an invoice cannot be reversed until the invoice is")
        void usedPrepaymentCannotBeReversed() {
            payment(1, "500.00");
            invoice(1, "300.00", 10);

            assertThatThrownBy(() -> settlementService.reverse(ref(PAYMENT, 1)))
                    .isInstanceOf(SettlementConflictException.class);

            settlementService.reverse(ref(INVOICE, 1));

            assertThat(openItems()).singleElement()
                    .satisfies(item -> assertThat(item.openAmount()).isEqualByComparingTo("-500.00"));
        }

        @Test
        @DisplayName("Reverse twice, then repost: the amount counts once")
        void reverseIsIdempotentAndRepostCountsOnce() {
            invoice(1, "1000.00", 10);
            settlementService.reverse(ref(INVOICE, 1));
            settlementService.reverse(ref(INVOICE, 1));

            invoice(1, "1000.00", 10);

            assertThat(balance()).isEqualByComparingTo("1000.00");
        }
    }

    @Test
    @DisplayName("Open items always equal the sum of all movements")
    void openItemsEqualMovementSum() {
        invoice(1, "300.00", 5);
        invoice(2, "700.00", 15);
        payment(1, "500.00");
        payment(2, "900.00");
        invoice(3, "250.00", 20);
        settlementService.reverse(ref(PAYMENT, 1));
        payment(3, "100.00", new PaymentAllocation(ref(INVOICE, 1), new BigDecimal("100.00")));

        BigDecimal movementSum = movementRepository.findAll().stream()
                .map(movement -> movement.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(balance()).isEqualByComparingTo(movementSum);
    }

    private void invoice(long id, String amount, int dueInDays) {
        settlementService.postInvoice(key, ref(INVOICE, id), LocalDateTime.now(),
                LocalDate.now().plusDays(dueInDays), new BigDecimal(amount));
    }

    private void payment(long id, String amount, PaymentAllocation... allocations) {
        settlementService.postPayment(key, ref(PAYMENT, id), LocalDateTime.now(),
                new BigDecimal(amount), List.of(allocations));
    }

    private BigDecimal balance() {
        return queryService.getPayables(null).stream()
                .map(CounterpartyPayable::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OpenItemResponse> openItems() {
        return queryService.getOpenItems(supplier.getId(), null);
    }

    private static DocumentRef ref(String type, long id) {
        return new DocumentRef(type, id);
    }
}

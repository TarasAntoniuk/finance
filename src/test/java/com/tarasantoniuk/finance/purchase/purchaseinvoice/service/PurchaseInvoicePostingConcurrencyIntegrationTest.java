package com.tarasantoniuk.finance.purchase.purchaseinvoice.service;

import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.document.exception.InvalidDocumentStatusException;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.item.entity.Item;
import com.tarasantoniuk.finance.core.item.enums.ItemType;
import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import com.tarasantoniuk.finance.core.item.repository.ItemRepository;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceLineRequest;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceRequest;
import com.tarasantoniuk.finance.settlement.repository.SettlementMovementRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A double-clicked "post": two requests for the same invoice at once, each in its own transaction.
 */
@DisplayName("Concurrent posting of one purchase invoice")
class PurchaseInvoicePostingConcurrencyIntegrationTest extends BaseIntegrationTest {

    private static final int REPETITIONS = 5;
    private static final long TIMEOUT_SECONDS = 30;

    @Autowired private PurchaseInvoiceService invoiceService;
    @Autowired private SettlementMovementRepository movementRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private TestDataFactoryCore factoryCore;
    @Autowired private TestDataCleanerCore cleanerCore;

    private Long invoiceId;

    @BeforeEach
    void setUp() {
        cleanerCore.cleanAll();
        Currency currency = factoryCore.createUAH();
        Organization organization = factoryCore.createDefaultOrganization(factoryCore.createUkraine());
        Counterparty supplier = factoryCore.createSupplierCounterparty();
        Item item = new Item();
        item.setCode("CONS-H");
        item.setName("Consulting");
        item.setType(ItemType.SERVICE);
        item.setUnitCode("HUR");
        item.setVatCategory(VatCategory.S);
        item.setVatRate(new BigDecimal("20.00"));
        itemRepository.save(item);

        invoiceId = invoiceService.create(new PurchaseInvoiceRequest(organization.getId(), supplier.getId(), "INV-1",
                LocalDate.now(), LocalDate.now().plusDays(30), null, currency.getId(), null, null,
                List.of(new PurchaseInvoiceLineRequest(item.getId(), null, BigDecimal.ONE, null,
                        new BigDecimal("100.00"), null, null)))).id();
    }

    @AfterEach
    void tearDown() {
        cleanerCore.cleanAll();
    }

    @RepeatedTest(REPETITIONS)
    void doublePostShouldOpenTheDebtOnce() throws Exception {
        List<Throwable> failures = runTogether(() -> invoiceService.post(invoiceId), () -> invoiceService.post(invoiceId));

        assertThat(failures).singleElement().isInstanceOf(InvalidDocumentStatusException.class);
        assertThat(movementRepository.findAll()).singleElement()
                .satisfies(movement -> assertThat(movement.getAmount()).isEqualByComparingTo("120.00"));
    }

    private static List<Throwable> runTogether(Runnable first, Runnable second) throws Exception {
        Authentication caller = SecurityContextHolder.getContext().getAuthentication();
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> futures = List.of(
                    executor.submit(() -> asCaller(caller, start, first)),
                    executor.submit(() -> asCaller(caller, start, second)));
            List<Throwable> failures = new ArrayList<>();
            for (Future<?> future : futures) {
                try {
                    future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    failures.add(e.getCause());
                }
            }
            return failures;
        } finally {
            executor.shutdownNow();
        }
    }

    private static Void asCaller(Authentication caller, CyclicBarrier start, Runnable call) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(caller);
        try {
            start.await();
            call.run();
            return null;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}

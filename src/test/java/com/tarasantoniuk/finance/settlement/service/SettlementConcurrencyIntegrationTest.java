package com.tarasantoniuk.finance.settlement.service;

import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.settlement.entity.SettlementMovement;
import com.tarasantoniuk.finance.settlement.entity.SettlementOpenItem;
import com.tarasantoniuk.finance.settlement.model.DocumentRef;
import com.tarasantoniuk.finance.settlement.model.SettlementKey;
import com.tarasantoniuk.finance.settlement.repository.SettlementMovementRepository;
import com.tarasantoniuk.finance.settlement.repository.SettlementOpenItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Two payments to the same supplier at the same moment, each committing on its own, as two
 * HTTP requests would. Without serialisation both read the same open amount and one update is lost.
 */
@DisplayName("Concurrent settlement postings")
class SettlementConcurrencyIntegrationTest extends BaseIntegrationTest {

    private static final int REPETITIONS = 5;
    private static final long TIMEOUT_SECONDS = 30;

    @Autowired private SettlementService settlementService;
    @Autowired private SettlementMovementRepository movementRepository;
    @Autowired private SettlementOpenItemRepository openItemRepository;
    @Autowired private TestDataFactoryCore factoryCore;
    @Autowired private TestDataCleanerCore cleanerCore;

    private SettlementKey key;

    @BeforeEach
    void setUp() {
        cleanerCore.cleanAll();
        Currency currency = factoryCore.createUAH();
        Organization organization = factoryCore.createDefaultOrganization(factoryCore.createUkraine());
        Counterparty supplier = factoryCore.createSupplierCounterparty();
        key = new SettlementKey(organization.getId(), supplier.getId(), currency.getId());
    }

    @AfterEach
    void tearDown() {
        cleanerCore.cleanAll();
    }

    @RepeatedTest(REPETITIONS)
    @DisplayName("Two payments against one invoice settle it once and leave the rest as a prepayment")
    void concurrentPaymentsKeepOpenItemsConsistent() throws Exception {
        settlementService.postInvoice(key, new DocumentRef("PurchaseInvoice", 1L), LocalDateTime.now(),
                LocalDate.now().plusDays(10), new BigDecimal("1000.00"));

        runTogether(() -> pay(1L), () -> pay(2L));

        List<SettlementOpenItem> openItems = openItemRepository.findAll();
        assertThat(openItems).singleElement().satisfies(item -> {
            assertThat(item.getSettlementDocumentType()).isEqualTo("BankPayment");
            assertThat(item.getOpenAmount()).isEqualByComparingTo("-400.00");
        });
        BigDecimal movementSum = movementRepository.findAll().stream()
                .map(SettlementMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(movementSum).isEqualByComparingTo("-400.00");
    }

    private void pay(long paymentId) {
        settlementService.postPayment(key, new DocumentRef("BankPayment", paymentId), LocalDateTime.now(),
                new BigDecimal("700.00"), List.of());
    }

    private static void runTogether(Runnable first, Runnable second) throws Exception {
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> futures = List.of(
                    executor.submit(() -> awaitAndRun(start, first)),
                    executor.submit(() -> awaitAndRun(start, second)));
            for (Future<?> future : futures) {
                future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static Void awaitAndRun(CyclicBarrier start, Runnable call) throws Exception {
        start.await();
        call.run();
        return null;
    }
}

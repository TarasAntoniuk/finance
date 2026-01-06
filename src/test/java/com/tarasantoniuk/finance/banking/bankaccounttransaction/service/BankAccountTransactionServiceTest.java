package com.tarasantoniuk.finance.banking.bankaccounttransaction.service;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccountbalance.service.BankAccountBalanceService;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import com.tarasantoniuk.finance.banking.bankaccounttransaction.enums.TransactionType;
import com.tarasantoniuk.finance.banking.common.TestDataCleanerBanking;
import com.tarasantoniuk.finance.banking.common.TestDataFactoryBanking;
import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BankAccountTransactionService Integration Tests")
class BankAccountTransactionServiceTest extends BaseIntegrationTest {

    @Autowired
    private BankAccountTransactionService service;

    @Autowired
    private BankAccountBalanceService balanceService;

    @Autowired
    private TestDataFactoryCore factoryCore;

    @Autowired
    private TestDataFactoryBanking factoryBanking;

    @Autowired
    private TestDataCleanerCore cleanerCore;

    @Autowired
    private TestDataCleanerBanking cleanerBanking;

    private Country country;
    private Currency currency;
    private Organization organization;
    private Bank bank;
    private BankAccount bankAccount;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

        // Create core entities
        country = factoryCore.createUkraine();
        currency = factoryCore.createUAH();
        organization = factoryCore.createDefaultOrganization(country);

        // Create banking entities
        bank = factoryBanking.createPrivatBank(country);
        bankAccount = factoryBanking.createOrganizationBankAccount(bank, currency, organization);
    }

    @AfterEach
    void tearDown() {
        cleanerBanking.cleanAll();
        cleanerCore.cleanAll();
    }



    @Nested
    @DisplayName("getAccountEvents Tests")
    class GetAccountEventsTests {

        @Test
        @DisplayName("Should return all events for account")
        void shouldReturnAllEvents() {
            // Given: Create multiple events
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST", 1L, "Receipt 1");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.plusHours(1), new BigDecimal("500.00"), TransactionType.CREDIT, "TEST", 2L, "Payment 1");

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEvents(bankAccount.getId());

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BankAccountTransactionEvent::getTransactionType)
                    .containsExactlyInAnyOrder(TransactionType.DEBIT, TransactionType.CREDIT);
        }

        @Test
        @DisplayName("Should return empty list when no events")
        void shouldReturnEmptyListWhenNoEvents() {
            // Given: No events created

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEvents(bankAccount.getId());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAccountEventsInDateTimeRange Tests")
    class GetAccountEventsInDateTimeRangeTests {

        @Test
        @DisplayName("Should return events within datetime range")
        void shouldReturnEventsInDateTimeRange() {
            // Given
            LocalDateTime startDateTime = testDateTime;
            LocalDateTime endDateTime = testDateTime.plusDays(5);
            LocalDateTime middleDateTime = testDateTime.plusDays(2);

            // Events within range
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    startDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST", 1L, "Receipt in range");

            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    middleDateTime, new BigDecimal("500.00"), TransactionType.CREDIT, "TEST", 2L, "Payment in range");

            // Event outside range
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    endDateTime.plusDays(10), new BigDecimal("2000.00"), TransactionType.DEBIT, "TEST", 3L, "Receipt outside range");

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEventsInDateTimeRange(
                    bankAccount.getId(), startDateTime, endDateTime);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(event ->
                    !event.getTransactionDateTime().isBefore(startDateTime) &&
                            !event.getTransactionDateTime().isAfter(endDateTime));
        }

        @Test
        @DisplayName("Should return empty list when no events in range")
        void shouldReturnEmptyListWhenNoEventsInRange() {
            // Given
            LocalDateTime startDateTime = testDateTime;
            LocalDateTime endDateTime = testDateTime.plusDays(5);

            // Create event outside range
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    endDateTime.plusDays(10), new BigDecimal("1000.00"), TransactionType.DEBIT, "TEST", 1L, "Receipt");

            // When
            List<BankAccountTransactionEvent> result = service.getAccountEventsInDateTimeRange(
                    bankAccount.getId(), startDateTime, endDateTime);

            // Then
            assertThat(result).isEmpty();
        }
    }


    @Nested
    @DisplayName("postDocument Tests")
    class PostDocumentTests {

        @Test
        @DisplayName("Should post BankReceipt document successfully")
        void shouldPostBankReceiptDocument() {
            // Given
            Long documentId = 100L;
            BigDecimal amount = new BigDecimal("1000.00");

            // When
            BankAccountTransactionEvent result = service.postDocument(
                    bankAccount.getId(),
                    organization.getId(),
                    currency.getId(),
                    testDateTime,
                    amount,
                    "Test receipt",
                    "BankReceipt",
                    documentId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDocumentType()).isEqualTo("BankReceipt");
            assertThat(result.getDocumentId()).isEqualTo(documentId);
            assertThat(result.getTransactionType()).isEqualTo(TransactionType.DEBIT);
            assertThat(result.getAmount()).isEqualByComparingTo(amount);
            assertThat(result.getBalanceAfter()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should post BankPayment document successfully")
        void shouldPostBankPaymentDocument() {
            // Given
            Long documentId = 200L;
            BigDecimal amount = new BigDecimal("500.00");

            // When
            BankAccountTransactionEvent result = service.postDocument(
                    bankAccount.getId(),
                    organization.getId(),
                    currency.getId(),
                    testDateTime,
                    amount,
                    "Test payment",
                    "BankPayment",
                    documentId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDocumentType()).isEqualTo("BankPayment");
            assertThat(result.getDocumentId()).isEqualTo(documentId);
            assertThat(result.getTransactionType()).isEqualTo(TransactionType.CREDIT);
            assertThat(result.getAmount()).isEqualByComparingTo(amount);
            assertThat(result.getBalanceAfter()).isEqualByComparingTo(amount.negate());
        }

        @Test
        @DisplayName("Should throw exception when posting duplicate document")
        void shouldThrowExceptionWhenPostingDuplicate() {
            // Given
            Long documentId = 300L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "First post", "BankReceipt", documentId);

            // When/Then
            assertThatThrownBy(() -> service.postDocument(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Second post", "BankReceipt", documentId))
                    .isInstanceOf(com.tarasantoniuk.finance.common.exception.ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Transaction event already exists for BankReceipt id: 300");
        }

        @Test
        @DisplayName("Should handle repost after unpost by marking reversal as reversed")
        void shouldHandleRepostAfterUnpost() {
            // Given: Post → Unpost → Repost cycle
            Long documentId = 400L;

            // 1. Initial post
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Initial post", "BankReceipt", documentId);

            // 2. Unpost (creates reversal event)
            service.reverseDocument("BankReceipt", documentId, "Initial post");

            // 3. Repost (should mark reversal as reversed)
            BankAccountTransactionEvent repostedEvent = service.postDocument(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Repost", "BankReceipt", documentId);

            // Then
            assertThat(repostedEvent).isNotNull();
            assertThat(repostedEvent.getIsReversed()).isFalse();

            // Verify reversal event is marked as reversed
            assertThat(service.existsByDocument("BankReceiptReversal", documentId)).isFalse();
        }
    }

    @Nested
    @DisplayName("reverseDocument Tests")
    class ReverseDocumentTests {

        @Test
        @DisplayName("Should reverse BankReceipt document successfully")
        void shouldReverseBankReceiptDocument() {
            // Given: Post a BankReceipt
            Long documentId = 500L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Original receipt", "BankReceipt", documentId);

            // When: Reverse it
            BankAccountTransactionEvent reversalEvent = service.reverseDocument(
                    "BankReceipt", documentId, "Original receipt");

            // Then
            assertThat(reversalEvent).isNotNull();
            assertThat(reversalEvent.getDocumentType()).isEqualTo("BankReceiptReversal");
            assertThat(reversalEvent.getDocumentId()).isEqualTo(documentId);
            assertThat(reversalEvent.getTransactionType()).isEqualTo(TransactionType.CREDIT); // Inverted
            assertThat(reversalEvent.getDescription()).contains("Reversal of receipt #500");
            assertThat(reversalEvent.getDescription()).contains("Original receipt");

            // Verify original event is marked as reversed
            BankAccountTransactionEvent originalEvent = service.findByDocument("BankReceipt", documentId);
            assertThat(originalEvent.getIsReversed()).isTrue();
            assertThat(originalEvent.getReversedByEventId()).isEqualTo(reversalEvent.getId());
        }

        @Test
        @DisplayName("Should reverse BankPayment document successfully")
        void shouldReverseBankPaymentDocument() {
            // Given: Post a BankPayment
            Long documentId = 600L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("500.00"), "Original payment", "BankPayment", documentId);

            // When: Reverse it
            BankAccountTransactionEvent reversalEvent = service.reverseDocument(
                    "BankPayment", documentId, "Original payment");

            // Then
            assertThat(reversalEvent).isNotNull();
            assertThat(reversalEvent.getDocumentType()).isEqualTo("BankPaymentReversal");
            assertThat(reversalEvent.getDocumentId()).isEqualTo(documentId);
            assertThat(reversalEvent.getTransactionType()).isEqualTo(TransactionType.DEBIT); // Inverted
            assertThat(reversalEvent.getDescription()).contains("Reversal of payment #600");
        }

        @Test
        @DisplayName("Should handle null description in reversal")
        void shouldHandleNullDescriptionInReversal() {
            // Given
            Long documentId = 700L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), null, "BankReceipt", documentId);

            // When
            BankAccountTransactionEvent reversalEvent = service.reverseDocument(
                    "BankReceipt", documentId, null);

            // Then
            assertThat(reversalEvent.getDescription()).isEqualTo("Reversal of receipt #700");
            assertThat(reversalEvent.getDescription()).doesNotContain(":");
        }

        @Test
        @DisplayName("Should handle blank description in reversal")
        void shouldHandleBlankDescriptionInReversal() {
            // Given
            Long documentId = 800L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "   ", "BankReceipt", documentId);

            // When
            BankAccountTransactionEvent reversalEvent = service.reverseDocument(
                    "BankReceipt", documentId, "   ");

            // Then
            assertThat(reversalEvent.getDescription()).isEqualTo("Reversal of receipt #800");
        }
    }

    @Nested
    @DisplayName("findByDocument Tests")
    class FindByDocumentTests {

        @Test
        @DisplayName("Should find document when it exists")
        void shouldFindDocumentWhenExists() {
            // Given
            Long documentId = 900L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);

            // When
            BankAccountTransactionEvent result = service.findByDocument("BankReceipt", documentId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDocumentId()).isEqualTo(documentId);
        }

        @Test
        @DisplayName("Should throw exception when document not found")
        void shouldThrowExceptionWhenDocumentNotFound() {
            // Given: No document posted

            // When/Then
            assertThatThrownBy(() -> service.findByDocument("BankReceipt", 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transaction event not found for document: BankReceipt #999");
        }
    }

    @Nested
    @DisplayName("findActiveByDocument Tests")
    class FindActiveByDocumentTests {

        @Test
        @DisplayName("Should find active document when not reversed")
        void shouldFindActiveDocumentWhenNotReversed() {
            // Given
            Long documentId = 1000L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);

            // When
            BankAccountTransactionEvent result = service.findActiveByDocument("BankReceipt", documentId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIsReversed()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when active document not found after reversal")
        void shouldThrowExceptionWhenActiveDocumentNotFoundAfterReversal() {
            // Given: Post and reverse
            Long documentId = 1100L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);
            service.reverseDocument("BankReceipt", documentId, "Test");

            // When/Then
            assertThatThrownBy(() -> service.findActiveByDocument("BankReceipt", documentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Active transaction event not found for document: BankReceipt #1100");
        }
    }

    @Nested
    @DisplayName("existsByDocument Tests")
    class ExistsByDocumentTests {

        @Test
        @DisplayName("Should return true when document exists and not reversed")
        void shouldReturnTrueWhenDocumentExistsAndNotReversed() {
            // Given
            Long documentId = 1200L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);

            // When
            boolean result = service.existsByDocument("BankReceipt", documentId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when document does not exist")
        void shouldReturnFalseWhenDocumentDoesNotExist() {
            // Given: No document posted

            // When
            boolean result = service.existsByDocument("BankReceipt", 999L);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when document is reversed")
        void shouldReturnFalseWhenDocumentIsReversed() {
            // Given: Post and reverse
            Long documentId = 1300L;
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Test", "BankReceipt", documentId);
            service.reverseDocument("BankReceipt", documentId, "Test");

            // When
            boolean result = service.existsByDocument("BankReceipt", documentId);

            // Then
            assertThat(result).isFalse(); // Reversed document should not exist as active
        }
    }

    @Nested
    @DisplayName("reverseTransaction Tests")
    class ReverseTransactionTests {

        @Test
        @DisplayName("Should establish bidirectional links between events")
        void shouldEstablishBidirectionalLinks() {
            // Given
            BankAccountTransactionEvent originalEvent = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Original");

            BankAccountTransactionEvent reversalEvent = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.CREDIT,
                    "TESTReversal", 1L, "Reversal");

            // When
            service.reverseTransaction(originalEvent.getId(), reversalEvent.getId());

            // Then
            BankAccountTransactionEvent updatedOriginal = service.findByDocument("TEST", 1L);
            BankAccountTransactionEvent updatedReversal = service.findByDocument("TESTReversal", 1L);

            assertThat(updatedOriginal.getIsReversed()).isTrue();
            assertThat(updatedOriginal.getReversedByEventId()).isEqualTo(reversalEvent.getId());
            assertThat(updatedReversal.getReversedByEventId()).isEqualTo(originalEvent.getId());
        }

        @Test
        @DisplayName("Should throw exception when original event not found")
        void shouldThrowExceptionWhenOriginalEventNotFound() {
            // Given
            BankAccountTransactionEvent reversalEvent = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.CREDIT,
                    "TESTReversal", 1L, "Reversal");

            // When/Then
            assertThatThrownBy(() -> service.reverseTransaction(999999L, reversalEvent.getId()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transaction event not found with id: 999999");
        }

        @Test
        @DisplayName("Should throw exception when reversal event not found")
        void shouldThrowExceptionWhenReversalEventNotFound() {
            // Given
            BankAccountTransactionEvent originalEvent = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Original");

            // When/Then
            assertThatThrownBy(() -> service.reverseTransaction(originalEvent.getId(), 999999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reversal event not found with id: 999999");
        }
    }

    @Nested
    @DisplayName("createTransactionEvent Tests")
    class CreateTransactionEventTests {

        @Test
        @DisplayName("Should throw exception when bank account not found")
        void shouldThrowExceptionWhenBankAccountNotFound() {
            // When/Then
            assertThatThrownBy(() -> service.createTransactionEvent(
                    999999L, organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Test"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Bank account not found with id: 999999");
        }

        @Test
        @DisplayName("Should throw exception when organization not found")
        void shouldThrowExceptionWhenOrganizationNotFound() {
            // When/Then
            assertThatThrownBy(() -> service.createTransactionEvent(
                    bankAccount.getId(), 999999L, currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Test"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Organization not found with id: 999999");
        }

        @Test
        @DisplayName("Should throw exception when currency not found")
        void shouldThrowExceptionWhenCurrencyNotFound() {
            // When/Then
            assertThatThrownBy(() -> service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), 999999L,
                    testDateTime, new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Test"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Currency not found with id: 999999");
        }

        @Test
        @DisplayName("Should calculate balance after for DEBIT transaction")
        void shouldCalculateBalanceAfterForDebit() {
            // Given: Existing balance
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.minusHours(1), new BigDecimal("500.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Previous");

            // When: Create new DEBIT
            BankAccountTransactionEvent result = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("300.00"), TransactionType.DEBIT,
                    "TEST", 2L, "New debit");

            // Then
            assertThat(result.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("800.00")); // 500 + 300
        }

        @Test
        @DisplayName("Should calculate balance after for CREDIT transaction")
        void shouldCalculateBalanceAfterForCredit() {
            // Given: Existing balance
            service.createTransactionEvent(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime.minusHours(1), new BigDecimal("1000.00"), TransactionType.DEBIT,
                    "TEST", 1L, "Previous");

            // When: Create new CREDIT
            BankAccountTransactionEvent result = service.createTransactionEvent(
                    bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("400.00"), TransactionType.CREDIT,
                    "TEST", 2L, "New credit");

            // Then
            assertThat(result.getBalanceAfter()).isEqualByComparingTo(new BigDecimal("600.00")); // 1000 - 400
        }
    }

    @Nested
    @DisplayName("Balance Calculation with Reversal Events Tests")
    class BalanceCalculationWithReversalTests {

        @Test
        @DisplayName("Should handle post-unpost-repost cycle correctly in balance")
        void shouldHandlePostUnpostRepostCycleInBalance() {
            // Given
            // 1. Post: +1000
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Receipt", "BankReceipt", 1L);

            BigDecimal balanceAfterPost = balanceService.getCurrentBalance(bankAccount.getId());
            assertThat(balanceAfterPost).isEqualByComparingTo(new BigDecimal("1000.00"));

            // 2. Unpost: creates reversal
            service.reverseDocument("BankReceipt", 1L, "Receipt");

            BigDecimal balanceAfterUnpost = balanceService.getCurrentBalance(bankAccount.getId());
            assertThat(balanceAfterUnpost).isEqualByComparingTo(BigDecimal.ZERO);

            // 3. Repost: creates new event, marks reversal as reversed
            service.postDocument(bankAccount.getId(), organization.getId(), currency.getId(),
                    testDateTime, new BigDecimal("1000.00"), "Receipt reposted", "BankReceipt", 1L);

            BigDecimal balanceAfterRepost = balanceService.getCurrentBalance(bankAccount.getId());
            assertThat(balanceAfterRepost).isEqualByComparingTo(new BigDecimal("1000.00"));
        }
    }
}
package com.tarasantoniuk.finance.settlement.service;

import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import com.tarasantoniuk.finance.settlement.entity.SettlementMovement;
import com.tarasantoniuk.finance.settlement.entity.SettlementOpenItem;
import com.tarasantoniuk.finance.settlement.exception.SettlementAllocationException;
import com.tarasantoniuk.finance.settlement.exception.SettlementConflictException;
import com.tarasantoniuk.finance.settlement.model.DocumentRef;
import com.tarasantoniuk.finance.settlement.model.PaymentAllocation;
import com.tarasantoniuk.finance.settlement.model.SettlementKey;
import com.tarasantoniuk.finance.settlement.repository.SettlementMovementRepository;
import com.tarasantoniuk.finance.settlement.repository.SettlementOpenItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Writes counterparty settlements. Called by the services of documents that change what is owed:
 * purchase invoices (we owe more), supplier payments (we owe less).
 * <p>
 * Every write locks the counterparty row first, so postings for one counterparty are serialised.
 * Movements are append-only; reversal writes storno rows. Open items are kept in step in the
 * same transaction.
 */
@Service
@Transactional
public class SettlementService {

    private final SettlementMovementRepository movementRepository;
    private final SettlementOpenItemRepository openItemRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrencyRepository currencyRepository;

    public SettlementService(SettlementMovementRepository movementRepository,
                             SettlementOpenItemRepository openItemRepository,
                             CounterpartyRepository counterpartyRepository,
                             OrganizationRepository organizationRepository,
                             CurrencyRepository currencyRepository) {
        this.movementRepository = movementRepository;
        this.openItemRepository = openItemRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.organizationRepository = organizationRepository;
        this.currencyRepository = currencyRepository;
    }

    /**
     * An invoice increases the debt by its amount, then consumes the counterparty's unused
     * prepayments, oldest first.
     */
    public void postInvoice(SettlementKey key, DocumentRef invoice, LocalDateTime dateTime,
                            LocalDate dueDate, BigDecimal amount) {
        requirePositive(amount, "Invoice amount");
        lockCounterparty(key.counterpartyId());

        SettlementDocument invoiceDocument = new SettlementDocument(invoice, dateTime.toLocalDate(), dueDate);
        write(key, invoiceDocument, dateTime, amount, invoice, null);
        offsetPrepayments(key, invoiceDocument, dateTime, amount);
    }

    /**
     * A payment reduces the debt. With explicit allocations it settles exactly those invoices;
     * without, it settles unpaid invoices oldest due date first. Whatever is left becomes a
     * prepayment, with the payment itself as the settlement document.
     */
    public void postPayment(SettlementKey key, DocumentRef payment, LocalDateTime dateTime,
                            BigDecimal amount, List<PaymentAllocation> allocations) {
        requirePositive(amount, "Payment amount");
        lockCounterparty(key.counterpartyId());

        BigDecimal remaining = allocations.isEmpty()
                ? allocateOldestFirst(key, payment, dateTime, amount)
                : allocateExplicitly(key, payment, dateTime, amount, allocations);

        if (remaining.signum() > 0) {
            SettlementDocument prepayment = new SettlementDocument(payment, dateTime.toLocalDate(), null);
            write(key, prepayment, dateTime, remaining.negate(), payment, null);
        }
    }

    /**
     * Storno of everything the document wrote. Refused while other documents have settled against
     * it (payments allocated to an invoice, an invoice that consumed a prepayment).
     */
    public void reverse(DocumentRef source) {
        List<SettlementMovement> movements = movementRepository
                .findBySourceDocumentTypeAndSourceDocumentIdOrderByIdAsc(source.type(), source.id());
        if (movements.isEmpty()) {
            return;
        }
        lockCounterparty(movements.getFirst().getCounterparty().getId());
        ensureNotSettledByOtherDocuments(source);

        Set<Long> alreadyReversed = movements.stream()
                .map(SettlementMovement::getStornoOfId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        movements.stream()
                .filter(movement -> movement.getStornoOfId() == null)
                .filter(movement -> !alreadyReversed.contains(movement.getId()))
                .forEach(this::writeStorno);
    }

    private BigDecimal allocateOldestFirst(SettlementKey key, DocumentRef payment,
                                           LocalDateTime dateTime, BigDecimal amount) {
        BigDecimal remaining = amount;
        for (SettlementOpenItem invoice : openItemRepository.findUnpaid(
                key.organizationId(), key.counterpartyId(), key.currencyId())) {
            if (remaining.signum() == 0) {
                break;
            }
            BigDecimal part = remaining.min(invoice.getOpenAmount());
            write(key, SettlementDocument.of(invoice), dateTime, part.negate(), payment, null);
            remaining = remaining.subtract(part);
        }
        return remaining;
    }

    private BigDecimal allocateExplicitly(SettlementKey key, DocumentRef payment, LocalDateTime dateTime,
                                          BigDecimal amount, List<PaymentAllocation> allocations) {
        BigDecimal allocated = BigDecimal.ZERO;
        for (PaymentAllocation allocation : allocations) {
            requirePositive(allocation.amount(), "Allocation amount");
            SettlementOpenItem invoice = findAllocatableInvoice(key, allocation);
            allocated = allocated.add(allocation.amount());
            if (allocated.compareTo(amount) > 0) {
                throw new SettlementAllocationException(
                        "Allocations exceed the payment amount " + amount);
            }
            write(key, SettlementDocument.of(invoice), dateTime, allocation.amount().negate(), payment, null);
        }
        return amount.subtract(allocated);
    }

    private SettlementOpenItem findAllocatableInvoice(SettlementKey key, PaymentAllocation allocation) {
        DocumentRef document = allocation.settlementDocument();
        SettlementOpenItem invoice = openItemRepository
                .findBySettlementDocumentTypeAndSettlementDocumentId(document.type(), document.id())
                .filter(item -> item.getOpenAmount().signum() > 0)
                .orElseThrow(() -> new SettlementAllocationException(
                        document.type() + " #" + document.id() + " has no unpaid amount"));
        if (!belongsTo(invoice, key)) {
            throw new SettlementAllocationException(document.type() + " #" + document.id()
                    + " belongs to another organization, counterparty or currency");
        }
        if (allocation.amount().compareTo(invoice.getOpenAmount()) > 0) {
            throw new SettlementAllocationException("Allocation " + allocation.amount() + " exceeds the unpaid "
                    + invoice.getOpenAmount() + " of " + document.type() + " #" + document.id());
        }
        return invoice;
    }

    private void offsetPrepayments(SettlementKey key, SettlementDocument invoice,
                                   LocalDateTime dateTime, BigDecimal invoiceAmount) {
        BigDecimal remaining = invoiceAmount;
        for (SettlementOpenItem prepayment : openItemRepository.findPrepayments(
                key.organizationId(), key.counterpartyId(), key.currencyId())) {
            if (remaining.signum() == 0) {
                break;
            }
            BigDecimal part = remaining.min(prepayment.getOpenAmount().negate());
            write(key, invoice, dateTime, part.negate(), invoice.ref(), null);
            write(key, SettlementDocument.of(prepayment), dateTime, part, invoice.ref(), null);
            remaining = remaining.subtract(part);
        }
    }

    private void ensureNotSettledByOtherDocuments(DocumentRef source) {
        BigDecimal settledByOthers = movementRepository.sumSettledByOtherDocuments(source.type(), source.id());
        if (settledByOthers.signum() != 0) {
            throw new SettlementConflictException(source.type() + " #" + source.id()
                    + " has settlements by other documents (" + settledByOthers.abs()
                    + "); reverse those documents first");
        }
    }

    private void writeStorno(SettlementMovement original) {
        SettlementKey key = new SettlementKey(original.getOrganization().getId(),
                original.getCounterparty().getId(), original.getCurrency().getId());
        SettlementDocument document = new SettlementDocument(
                new DocumentRef(original.getSettlementDocumentType(), original.getSettlementDocumentId()),
                original.getSettlementDocumentDate(), original.getSettlementDueDate());
        DocumentRef source = new DocumentRef(original.getSourceDocumentType(), original.getSourceDocumentId());
        write(key, document, original.getMovementDateTime(), original.getAmount().negate(), source, original.getId());
    }

    private void write(SettlementKey key, SettlementDocument document, LocalDateTime dateTime,
                       BigDecimal amount, DocumentRef source, Long stornoOfId) {
        SettlementMovement movement = new SettlementMovement();
        movement.setOrganization(organizationRepository.getReferenceById(key.organizationId()));
        movement.setCounterparty(counterpartyRepository.getReferenceById(key.counterpartyId()));
        movement.setCurrency(currencyRepository.getReferenceById(key.currencyId()));
        movement.setSettlementDocumentType(document.ref().type());
        movement.setSettlementDocumentId(document.ref().id());
        movement.setSettlementDocumentDate(document.documentDate());
        movement.setSettlementDueDate(document.dueDate());
        movement.setMovementDateTime(dateTime);
        movement.setAmount(amount);
        movement.setSourceDocumentType(source.type());
        movement.setSourceDocumentId(source.id());
        movement.setStornoOfId(stornoOfId);
        movementRepository.save(movement);

        applyToOpenItem(key, document, amount);
    }

    private void applyToOpenItem(SettlementKey key, SettlementDocument document, BigDecimal amount) {
        SettlementOpenItem item = openItemRepository
                .findBySettlementDocumentTypeAndSettlementDocumentId(document.ref().type(), document.ref().id())
                .orElseGet(() -> newOpenItem(key, document));
        BigDecimal openAmount = item.getOpenAmount().add(amount);
        if (openAmount.signum() == 0) {
            if (item.getId() != null) {
                openItemRepository.delete(item);
            }
            return;
        }
        item.setOpenAmount(openAmount);
        openItemRepository.save(item);
    }

    private SettlementOpenItem newOpenItem(SettlementKey key, SettlementDocument document) {
        SettlementOpenItem item = new SettlementOpenItem();
        item.setOrganization(organizationRepository.getReferenceById(key.organizationId()));
        item.setCounterparty(counterpartyRepository.getReferenceById(key.counterpartyId()));
        item.setCurrency(currencyRepository.getReferenceById(key.currencyId()));
        item.setSettlementDocumentType(document.ref().type());
        item.setSettlementDocumentId(document.ref().id());
        item.setDocumentDate(document.documentDate());
        item.setDueDate(document.dueDate());
        item.setOpenAmount(BigDecimal.ZERO);
        return item;
    }

    private void lockCounterparty(Long counterpartyId) {
        counterpartyRepository.findByIdForUpdate(counterpartyId)
                .orElseThrow(() -> new ResourceNotFoundException("Counterparty", "id", counterpartyId));
    }

    private static boolean belongsTo(SettlementOpenItem item, SettlementKey key) {
        return item.getOrganization().getId().equals(key.organizationId())
                && item.getCounterparty().getId().equals(key.counterpartyId())
                && item.getCurrency().getId().equals(key.currencyId());
    }

    private static void requirePositive(BigDecimal amount, String what) {
        if (amount == null || amount.signum() <= 0) {
            throw new SettlementAllocationException(what + " must be positive");
        }
    }

    private record SettlementDocument(DocumentRef ref, LocalDate documentDate, LocalDate dueDate) {

        static SettlementDocument of(SettlementOpenItem item) {
            return new SettlementDocument(
                    new DocumentRef(item.getSettlementDocumentType(), item.getSettlementDocumentId()),
                    item.getDocumentDate(), item.getDueDate());
        }
    }
}

package com.tarasantoniuk.finance.purchase.purchaseinvoice.service;

import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.common.document.exception.InvalidDocumentStatusException;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.common.exception.ResourceNotFoundException;
import com.tarasantoniuk.finance.common.exception.ValidationException;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.repository.CounterpartyRepository;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import com.tarasantoniuk.finance.core.item.entity.Item;
import com.tarasantoniuk.finance.core.item.exception.ItemNotFoundException;
import com.tarasantoniuk.finance.core.item.repository.ItemRepository;
import com.tarasantoniuk.finance.core.organization.repository.OrganizationRepository;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceLineRequest;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceRequest;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceResponse;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.dto.PurchaseInvoiceSummary;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.entity.PurchaseInvoice;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.entity.PurchaseInvoiceLine;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.exception.DuplicateSupplierInvoiceException;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.exception.PurchaseInvoiceNotFoundException;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.mapper.PurchaseInvoiceMapper;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.repository.PurchaseInvoiceRepository;
import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
import com.tarasantoniuk.finance.settlement.model.DocumentRef;
import com.tarasantoniuk.finance.settlement.model.SettlementKey;
import com.tarasantoniuk.finance.settlement.service.SettlementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase invoices: DRAFT documents are edited freely; posting opens the debt to the supplier in
 * counterparty settlements, unposting reverses it. Every status change and edit locks the invoice row.
 */
@Service
@Transactional
public class PurchaseInvoiceService {

    private final PurchaseInvoiceRepository invoiceRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final CurrencyRepository currencyRepository;
    private final ItemRepository itemRepository;
    private final OrganizationRepository organizationRepository;
    private final SettlementService settlementService;
    private final OrganizationSecurityContext orgContext;
    private final PurchaseInvoiceMapper mapper;

    public PurchaseInvoiceService(PurchaseInvoiceRepository invoiceRepository,
                                  CounterpartyRepository counterpartyRepository,
                                  CurrencyRepository currencyRepository,
                                  ItemRepository itemRepository,
                                  OrganizationRepository organizationRepository,
                                  SettlementService settlementService,
                                  OrganizationSecurityContext orgContext,
                                  PurchaseInvoiceMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.currencyRepository = currencyRepository;
        this.itemRepository = itemRepository;
        this.organizationRepository = organizationRepository;
        this.settlementService = settlementService;
        this.orgContext = orgContext;
        this.mapper = mapper;
    }

    public PurchaseInvoiceResponse create(PurchaseInvoiceRequest request) {
        Long organizationId = orgContext.resolveOrganizationId(request.organizationId());
        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setOrganization(organizationRepository.getReferenceById(organizationId));
        apply(request, invoice, organizationId);
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    public PurchaseInvoiceResponse update(Long id, PurchaseInvoiceRequest request) {
        PurchaseInvoice invoice = findForChange(id);
        requireStatus(invoice, DocumentStatus.DRAFT, "update");
        apply(request, invoice, invoice.getOrganization().getId());
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    public void delete(Long id) {
        PurchaseInvoice invoice = findForChange(id);
        requireStatus(invoice, DocumentStatus.DRAFT, "delete");
        invoiceRepository.delete(invoice);
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceResponse getById(Long id) {
        return mapper.toResponse(findWithDetails(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseInvoiceSummary> search(Long supplierId, DocumentStatus status, int page, int size) {
        Long organizationScope = orgContext.isAdmin() ? null : orgContext.getActiveOrganizationId();
        Page<PurchaseInvoice> invoices = invoiceRepository.search(organizationScope, supplierId, status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "issueDate", "id")));
        return PageResponse.of(invoices, invoices.map(mapper::toSummary).getContent());
    }

    public PurchaseInvoiceResponse post(Long id) {
        PurchaseInvoice invoice = findForChange(id);
        requireStatus(invoice, DocumentStatus.DRAFT, "post");
        if (invoice.getTotalGross().signum() <= 0) {
            throw new ValidationException("Invoice total must be positive to post");
        }

        settlementService.postInvoice(settlementKey(invoice), documentRef(invoice),
                invoice.getTransactionDateTime(), invoice.getDueDate(), invoice.getTotalGross());

        invoice.setStatus(DocumentStatus.POSTED);
        invoice.setPostedAt(LocalDateTime.now());
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    /**
     * Refused with 409 by settlements while payments are allocated to the invoice.
     */
    public PurchaseInvoiceResponse unpost(Long id) {
        PurchaseInvoice invoice = findForChange(id);
        requireStatus(invoice, DocumentStatus.POSTED, "unpost");

        settlementService.reverse(documentRef(invoice));

        invoice.setStatus(DocumentStatus.DRAFT);
        invoice.setPostedAt(null);
        return mapper.toResponse(invoiceRepository.save(invoice));
    }

    private void apply(PurchaseInvoiceRequest request, PurchaseInvoice invoice, Long organizationId) {
        if (request.dueDate() != null && request.dueDate().isBefore(request.issueDate())) {
            throw new ValidationException("Due date " + request.dueDate()
                    + " is before the issue date " + request.issueDate());
        }
        if (invoiceRepository.existsSupplierInvoiceNumber(organizationId, request.supplierId(),
                request.supplierInvoiceNumber(), invoice.getId())) {
            throw new DuplicateSupplierInvoiceException(request.supplierInvoiceNumber());
        }

        invoice.setSupplier(findSupplier(request.supplierId()));
        invoice.setCurrency(currencyRepository.findById(request.currencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "id", request.currencyId())));
        invoice.setSupplierInvoiceNumber(request.supplierInvoiceNumber());
        invoice.setIssueDate(request.issueDate());
        invoice.setDueDate(request.dueDate());
        invoice.setTransactionDateTime(request.postingDateTime() != null
                ? request.postingDateTime()
                : request.issueDate().atStartOfDay());
        invoice.setBuyerReference(request.buyerReference());
        invoice.setDescription(request.note());
        invoice.replaceLines(toLines(request.lines()));
    }

    private Counterparty findSupplier(Long supplierId) {
        Counterparty supplier = counterpartyRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Counterparty", "id", supplierId));
        if (supplier.getType() == Counterparty.CounterpartyType.CUSTOMER) {
            throw new ValidationException("Counterparty " + supplier.getCode() + " is a customer, not a supplier");
        }
        if (!Boolean.TRUE.equals(supplier.getIsActive())) {
            throw new ValidationException("Counterparty " + supplier.getCode() + " is inactive");
        }
        return supplier;
    }

    private List<PurchaseInvoiceLine> toLines(List<PurchaseInvoiceLineRequest> requests) {
        List<PurchaseInvoiceLine> lines = new ArrayList<>(requests.size());
        for (int index = 0; index < requests.size(); index++) {
            lines.add(toLine(requests.get(index), index + 1));
        }
        return lines;
    }

    private PurchaseInvoiceLine toLine(PurchaseInvoiceLineRequest request, int lineNumber) {
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new ItemNotFoundException(request.itemId()));
        if (!Boolean.TRUE.equals(item.getIsActive())) {
            throw new ValidationException("Line " + lineNumber + ": item " + item.getCode() + " is inactive");
        }
        if ((request.vatCategory() == null) != (request.vatRate() == null)) {
            throw new ValidationException("Line " + lineNumber + ": give VAT category and rate together, or neither");
        }

        PurchaseInvoiceLine line = new PurchaseInvoiceLine();
        line.setItem(item);
        line.setDescription(request.description());
        line.setQuantity(request.quantity());
        line.setUnitPriceNet(request.unitPriceNet());
        line.setUnitCode(request.unitCode() != null ? request.unitCode() : item.getUnitCode());
        line.setVatCategory(request.vatCategory() != null ? request.vatCategory() : item.getVatCategory());
        line.setVatRate(request.vatRate() != null ? request.vatRate() : item.getVatRate());
        if (!line.getVatCategory().isConsistentWith(line.getVatRate())) {
            throw new ValidationException("Line " + lineNumber + ": VAT category " + line.getVatCategory()
                    + " does not allow rate " + line.getVatRate());
        }
        return line;
    }

    private PurchaseInvoice findForChange(Long id) {
        invoiceRepository.findByIdForUpdate(id).orElseThrow(() -> new PurchaseInvoiceNotFoundException(id));
        return findWithDetails(id);
    }

    private PurchaseInvoice findWithDetails(Long id) {
        PurchaseInvoice invoice = invoiceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new PurchaseInvoiceNotFoundException(id));
        orgContext.validateAccess(invoice.getOrganization().getId());
        return invoice;
    }

    private static void requireStatus(PurchaseInvoice invoice, DocumentStatus expected, String operation) {
        if (invoice.getStatus() != expected) {
            throw new InvalidDocumentStatusException(invoice.getStatus(), expected, operation);
        }
    }

    private static SettlementKey settlementKey(PurchaseInvoice invoice) {
        return new SettlementKey(invoice.getOrganization().getId(), invoice.getSupplier().getId(),
                invoice.getCurrency().getId());
    }

    private static DocumentRef documentRef(PurchaseInvoice invoice) {
        return new DocumentRef(PurchaseInvoice.DOCUMENT_TYPE, invoice.getId());
    }
}

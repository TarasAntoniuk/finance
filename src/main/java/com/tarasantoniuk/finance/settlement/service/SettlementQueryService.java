package com.tarasantoniuk.finance.settlement.service;

import com.tarasantoniuk.finance.security.authorization.OrganizationSecurityContext;
import com.tarasantoniuk.finance.settlement.dto.CounterpartyPayable;
import com.tarasantoniuk.finance.settlement.dto.OpenItemResponse;
import com.tarasantoniuk.finance.settlement.entity.SettlementOpenItem;
import com.tarasantoniuk.finance.settlement.repository.SettlementOpenItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Reads counterparty settlements, scoped to the caller's organization (ADMIN may see all).
 * Both queries read only the open items, so their cost does not grow with the movement history.
 */
@Service
@Transactional(readOnly = true)
public class SettlementQueryService {

    private final SettlementOpenItemRepository openItemRepository;
    private final OrganizationSecurityContext orgContext;

    public SettlementQueryService(SettlementOpenItemRepository openItemRepository,
                                  OrganizationSecurityContext orgContext) {
        this.openItemRepository = openItemRepository;
        this.orgContext = orgContext;
    }

    public List<CounterpartyPayable> getPayables(Long requestedOrganizationId) {
        Long organizationId = orgContext.resolveOptionalOrganizationId(requestedOrganizationId);
        return openItemRepository.findPayables(organizationId, LocalDate.now());
    }

    public List<OpenItemResponse> getOpenItems(Long counterpartyId, Long requestedOrganizationId) {
        Long organizationId = orgContext.resolveOptionalOrganizationId(requestedOrganizationId);
        LocalDate today = LocalDate.now();
        return openItemRepository.findOpenItems(organizationId, counterpartyId).stream()
                .map(item -> toResponse(item, today))
                .toList();
    }

    private static OpenItemResponse toResponse(SettlementOpenItem item, LocalDate today) {
        boolean overdue = item.getOpenAmount().signum() > 0
                && item.getDueDate() != null
                && item.getDueDate().isBefore(today);
        return new OpenItemResponse(
                item.getSettlementDocumentType(),
                item.getSettlementDocumentId(),
                item.getCurrency().getCode(),
                item.getDocumentDate(),
                item.getDueDate(),
                item.getOpenAmount(),
                overdue);
    }
}

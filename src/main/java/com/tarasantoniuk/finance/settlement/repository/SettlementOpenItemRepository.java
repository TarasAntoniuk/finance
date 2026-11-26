package com.tarasantoniuk.finance.settlement.repository;

import com.tarasantoniuk.finance.settlement.dto.CounterpartyPayable;
import com.tarasantoniuk.finance.settlement.entity.SettlementOpenItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SettlementOpenItemRepository extends JpaRepository<SettlementOpenItem, Long> {

    Optional<SettlementOpenItem> findBySettlementDocumentTypeAndSettlementDocumentId(String type, Long id);

    /**
     * Unpaid invoices of one key, oldest due date first (no due date last): the FIFO order in which
     * payments are allocated.
     */
    @Query("""
            SELECT o FROM SettlementOpenItem o
            WHERE o.organization.id = :organizationId AND o.counterparty.id = :counterpartyId
              AND o.currency.id = :currencyId AND o.openAmount > 0
            ORDER BY o.dueDate ASC NULLS LAST, o.documentDate ASC, o.id ASC
            """)
    List<SettlementOpenItem> findUnpaid(@Param("organizationId") Long organizationId,
                                                 @Param("counterpartyId") Long counterpartyId,
                                                 @Param("currencyId") Long currencyId);

    /**
     * Unused prepayments of one key, oldest first.
     */
    @Query("""
            SELECT o FROM SettlementOpenItem o
            WHERE o.organization.id = :organizationId AND o.counterparty.id = :counterpartyId
              AND o.currency.id = :currencyId AND o.openAmount < 0
            ORDER BY o.documentDate ASC, o.id ASC
            """)
    List<SettlementOpenItem> findPrepayments(@Param("organizationId") Long organizationId,
                                                      @Param("counterpartyId") Long counterpartyId,
                                                      @Param("currencyId") Long currencyId);

    @Query("""
            SELECT new com.tarasantoniuk.finance.settlement.dto.CounterpartyPayable(
                o.organization.id, o.counterparty.id, o.counterparty.name, o.currency.code,
                SUM(o.openAmount),
                SUM(CASE WHEN o.openAmount > 0 AND o.dueDate < :today THEN o.openAmount ELSE 0 END),
                COUNT(o))
            FROM SettlementOpenItem o
            WHERE (:organizationId IS NULL OR o.organization.id = :organizationId)
            GROUP BY o.organization.id, o.counterparty.id, o.counterparty.name, o.currency.code
            ORDER BY o.counterparty.name, o.currency.code
            """)
    List<CounterpartyPayable> findPayables(@Param("organizationId") Long organizationId,
                                           @Param("today") LocalDate today);

    @Query("""
            SELECT o FROM SettlementOpenItem o
            JOIN FETCH o.currency
            WHERE o.counterparty.id = :counterpartyId
              AND (:organizationId IS NULL OR o.organization.id = :organizationId)
            ORDER BY o.dueDate ASC NULLS LAST, o.documentDate ASC, o.id ASC
            """)
    List<SettlementOpenItem> findOpenItems(@Param("organizationId") Long organizationId,
                                           @Param("counterpartyId") Long counterpartyId);
}

package com.tarasantoniuk.finance.settlement.repository;

import com.tarasantoniuk.finance.settlement.entity.SettlementMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SettlementMovementRepository extends JpaRepository<SettlementMovement, Long> {

    List<SettlementMovement> findBySourceDocumentTypeAndSourceDocumentIdOrderByIdAsc(String type, Long id);

    /**
     * Net amount other documents have settled against this settlement document
     * (payments allocated to an invoice, invoices that consumed a prepayment).
     */
    @Query("""
            SELECT COALESCE(SUM(m.amount), 0) FROM SettlementMovement m
            WHERE m.settlementDocumentType = :type AND m.settlementDocumentId = :id
              AND NOT (m.sourceDocumentType = :type AND m.sourceDocumentId = :id)
            """)
    BigDecimal sumSettledByOtherDocuments(@Param("type") String type, @Param("id") Long id);
}

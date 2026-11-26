package com.tarasantoniuk.finance.purchase.purchaseinvoice.repository;

import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import com.tarasantoniuk.finance.purchase.purchaseinvoice.entity.PurchaseInvoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {

    @Query("""
            SELECT DISTINCT i FROM PurchaseInvoice i
            JOIN FETCH i.organization
            JOIN FETCH i.supplier
            JOIN FETCH i.currency
            LEFT JOIN FETCH i.lines l
            LEFT JOIN FETCH l.item
            WHERE i.id = :id
            """)
    Optional<PurchaseInvoice> findByIdWithDetails(@Param("id") Long id);

    /**
     * Row lock held until the transaction ends; serialises edits and status changes of one invoice.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM PurchaseInvoice i WHERE i.id = :id")
    Optional<PurchaseInvoice> findByIdForUpdate(@Param("id") Long id);

    @Query(value = """
            SELECT i FROM PurchaseInvoice i
            JOIN FETCH i.organization
            JOIN FETCH i.supplier
            JOIN FETCH i.currency
            WHERE (:organizationId IS NULL OR i.organization.id = :organizationId)
              AND (:supplierId IS NULL OR i.supplier.id = :supplierId)
              AND (:status IS NULL OR i.status = :status)
            """,
            countQuery = """
            SELECT COUNT(i) FROM PurchaseInvoice i
            WHERE (:organizationId IS NULL OR i.organization.id = :organizationId)
              AND (:supplierId IS NULL OR i.supplier.id = :supplierId)
              AND (:status IS NULL OR i.status = :status)
            """)
    Page<PurchaseInvoice> search(@Param("organizationId") Long organizationId,
                                 @Param("supplierId") Long supplierId,
                                 @Param("status") DocumentStatus status,
                                 Pageable pageable);

    @Query("""
            SELECT COUNT(i) > 0 FROM PurchaseInvoice i
            WHERE i.organization.id = :organizationId AND i.supplier.id = :supplierId
              AND i.supplierInvoiceNumber = :number AND (:excludedId IS NULL OR i.id <> :excludedId)
            """)
    boolean existsSupplierInvoiceNumber(@Param("organizationId") Long organizationId,
                                        @Param("supplierId") Long supplierId,
                                        @Param("number") String number,
                                        @Param("excludedId") Long excludedId);
}

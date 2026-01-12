package com.tarasantoniuk.finance.banking.bankaccounttransaction.repository;

import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountTransactionEventRepository extends JpaRepository<BankAccountTransactionEvent, Long> {

    /**
     * Find all events for a specific bank account with all relations loaded (N+1 optimization)
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.bankAccount.id = :bankAccountId
            ORDER BY e.transactionDateTime ASC, e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findByBankAccountIdWithRelations(@Param("bankAccountId") Long bankAccountId);

    /**
     * Find events for a bank account within datetime range with all relations loaded.
     * Uses >= startDateTime AND < endDateTime for precise boundary control.
     * This ensures transactions at exact endDateTime are excluded (for opening balance calculation).
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.bankAccount.id = :bankAccountId
            AND e.transactionDateTime >= :startDateTime
            AND e.transactionDateTime < :endDateTime
            AND e.isReversed = false
            ORDER BY e.transactionDateTime ASC, e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findByBankAccountIdAndDateTimeRangeWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * Find events for a bank account up to specific datetime (inclusive)
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.bankAccount.id = :bankAccountId
            AND e.transactionDateTime <= :untilDateTime
            AND e.isReversed = false
            ORDER BY e.transactionDateTime ASC, e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findByBankAccountIdUntilDateTimeWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("untilDateTime") LocalDateTime untilDateTime
    );

    /**
     * Find events for a bank account after specific datetime with all relations loaded
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.bankAccount.id = :bankAccountId
            AND e.transactionDateTime > :afterDateTime
            AND e.isReversed = false
            ORDER BY e.transactionDateTime ASC, e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findByBankAccountIdAfterDateTimeWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("afterDateTime") LocalDateTime afterDateTime
    );

    /**
     * Find event by document type and document ID
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.documentType = :documentType
            AND e.documentId = :documentId
            """)
    Optional<BankAccountTransactionEvent> findByDocumentTypeAndDocumentIdWithRelations(
            @Param("documentType") String documentType,
            @Param("documentId") Long documentId
    );

    /**
     * Find active (non-reversed) transaction event by document type and ID
     * Uses explicit query to ensure only one event is returned
     */
    @Query("""
    SELECT te FROM BankAccountTransactionEvent te
    WHERE te.documentType = :documentType
    AND te.documentId = :documentId
    AND te.isReversed = false
    ORDER BY te.createdAt DESC
    LIMIT 1
    """)
    Optional<BankAccountTransactionEvent> findActiveByDocumentTypeAndDocumentId(
            @Param("documentType") String documentType,
            @Param("documentId") Long documentId);


    /**
     * Find all events by document type and document ID (for transfers there might be 2 events)
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.documentType = :documentType
            AND e.documentId = :documentId
            ORDER BY e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findAllByDocumentTypeAndDocumentIdWithRelations(
            @Param("documentType") String documentType,
            @Param("documentId") Long documentId
    );

    /**
     * Find events for organization within datetime range
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.organization.id = :organizationId
            AND e.transactionDateTime BETWEEN :startDateTime AND :endDateTime
            AND e.isReversed = false
            ORDER BY e.transactionDateTime ASC, e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findByOrganizationIdAndDateTimeRangeWithRelations(
            @Param("organizationId") Long organizationId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * Check if event exists for document
     */
    boolean existsByDocumentTypeAndDocumentId(String documentType, Long documentId);

    /**
     * Count non-reversed events for bank account
     */
    @Query("""
            SELECT COUNT(e) FROM BankAccountTransactionEvent e
            WHERE e.bankAccount.id = :bankAccountId
            AND e.isReversed = false
            """)
    long countNonReversedByBankAccountId(@Param("bankAccountId") Long bankAccountId);

    /**
     * Check if non-reversed event exists for document
     */
    boolean existsByDocumentTypeAndDocumentIdAndIsReversedFalse(String documentType, Long documentId);
}
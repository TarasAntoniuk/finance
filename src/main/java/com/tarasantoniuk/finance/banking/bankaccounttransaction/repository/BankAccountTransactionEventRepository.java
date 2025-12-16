package com.tarasantoniuk.finance.banking.bankaccounttransaction.repository;

import com.tarasantoniuk.finance.banking.bankaccounttransaction.entity.BankAccountTransactionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
            ORDER BY e.transactionDate ASC, e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findByBankAccountIdWithRelations(@Param("bankAccountId") Long bankAccountId);

    /**
     * Find events for a bank account within date range with all relations loaded
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.bankAccount.id = :bankAccountId
            AND e.transactionDate BETWEEN :startDate AND :endDate
            AND e.isReversed = false
            ORDER BY e.transactionDate ASC, e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findByBankAccountIdAndDateRangeWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find events for a bank account after specific date with all relations loaded
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.bankAccount.id = :bankAccountId
            AND e.transactionDate >= :afterDate
            AND e.isReversed = false
            ORDER BY e.transactionDate ASC, e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findByBankAccountIdAfterDateWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("afterDate") LocalDate afterDate
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
     * Find events for organization within date range
     */
    @Query("""
            SELECT e FROM BankAccountTransactionEvent e
            LEFT JOIN FETCH e.bankAccount
            LEFT JOIN FETCH e.organization
            LEFT JOIN FETCH e.currency
            WHERE e.organization.id = :organizationId
            AND e.transactionDate BETWEEN :startDate AND :endDate
            AND e.isReversed = false
            ORDER BY e.transactionDate ASC, e.createdAt ASC
            """)
    List<BankAccountTransactionEvent> findByOrganizationIdAndDateRangeWithRelations(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
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
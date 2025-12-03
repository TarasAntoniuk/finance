package com.tarasantoniuk.finance.banking.bankreceipt.repository;

import com.tarasantoniuk.finance.banking.bankreceipt.entity.BankReceipt;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BankReceiptRepository extends JpaRepository<BankReceipt, Long> {

    /**
     * Find receipt by ID with all related entities fetched (N+1 optimization)
     */
    @Query("""
                SELECT br FROM BankReceipt br
                LEFT JOIN FETCH br.account a
                LEFT JOIN FETCH a.bank
                LEFT JOIN FETCH a.currency
                LEFT JOIN FETCH br.organization
                LEFT JOIN FETCH br.counterparty cp
                LEFT JOIN FETCH br.counterpartyBankAccount cpa
                LEFT JOIN FETCH br.currency
                WHERE br.id = :id
            """)
    Optional<BankReceipt> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find all receipts with pagination and related entities (N+1 optimization)
     */
    @Query(value = """
            SELECT DISTINCT br FROM BankReceipt br
            LEFT JOIN FETCH br.account a
            LEFT JOIN FETCH a.bank
            LEFT JOIN FETCH a.currency
            LEFT JOIN FETCH br.organization
            LEFT JOIN FETCH br.counterparty
            LEFT JOIN FETCH br.currency
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT br) FROM BankReceipt br
                    """)
    Page<BankReceipt> findAllWithDetails(Pageable pageable);

    /**
     * Find receipts by account ID with pagination
     */
    @Query("""
                SELECT DISTINCT br FROM BankReceipt br
                LEFT JOIN FETCH br.account a
                LEFT JOIN FETCH a.bank
                LEFT JOIN FETCH br.counterparty
                LEFT JOIN FETCH br.currency
                WHERE a.id = :accountId
            """)
    Page<BankReceipt> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    /**
     * Find receipts by counterparty ID with pagination
     */
    @Query("""
                SELECT DISTINCT br FROM BankReceipt br
                LEFT JOIN FETCH br.account
                LEFT JOIN FETCH br.counterparty cp
                LEFT JOIN FETCH br.currency
                WHERE cp.id = :counterpartyId
            """)
    Page<BankReceipt> findByCounterpartyId(@Param("counterpartyId") Long counterpartyId, Pageable pageable);

    /**
     * Find receipts by status
     */
    @Query("""
                SELECT DISTINCT br FROM BankReceipt br
                LEFT JOIN FETCH br.account
                LEFT JOIN FETCH br.counterparty
                LEFT JOIN FETCH br.currency
                WHERE br.status = :status
            """)
    Page<BankReceipt> findByStatus(@Param("status") DocumentStatus status, Pageable pageable);

    /**
     * Find receipts by document date range
     */
    @Query("""
                SELECT DISTINCT br FROM BankReceipt br
                LEFT JOIN FETCH br.account
                LEFT JOIN FETCH br.counterparty
                LEFT JOIN FETCH br.currency
                WHERE br.documentDate BETWEEN :startDate AND :endDate
            """)
    Page<BankReceipt> findByDocumentDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Check if receipt with external transaction ID exists (for idempotency)
     */
    boolean existsByExternalTransactionId(String externalTransactionId);

    /**
     * Find receipt by external transaction ID with details (for idempotency check and retrieval)
     */
    @Query("""
                SELECT br FROM BankReceipt br
                LEFT JOIN FETCH br.account
                LEFT JOIN FETCH br.counterparty
                LEFT JOIN FETCH br.currency
                WHERE br.externalTransactionId = :externalTransactionId
            """)
    Optional<BankReceipt> findByExternalTransactionId(
            @Param("externalTransactionId") String externalTransactionId
    );
}
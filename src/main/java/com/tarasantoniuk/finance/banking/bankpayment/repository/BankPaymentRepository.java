package com.tarasantoniuk.finance.banking.bankpayment.repository;

import com.tarasantoniuk.finance.banking.bankpayment.entity.BankPayment;
import com.tarasantoniuk.finance.common.document.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BankPaymentRepository extends JpaRepository<BankPayment, Long> {

    /**
     * Find payment by ID with all related entities loaded (N+1 optimization)
     */
    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization
            WHERE p.id = :id
            """)
    Optional<BankPayment> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find all payments with pagination and N+1 optimization
     */
    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization
            """)
    Page<BankPayment> findAllWithDetails(Pageable pageable);

    /**
     * Find payments by account ID
     */
    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization
            WHERE p.account.id = :accountId
            """)
    Page<BankPayment> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    /**
     * Find payments by counterparty ID
     */
    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization
            WHERE p.counterparty.id = :counterpartyId
            """)
    Page<BankPayment> findByCounterpartyId(@Param("counterpartyId") Long counterpartyId, Pageable pageable);

    /**
     * Find payments by status
     */
    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization
            WHERE p.status = :status
            """)
    Page<BankPayment> findByStatus(@Param("status") DocumentStatus status, Pageable pageable);

    /**
     * Find payments by transaction datetime range
     */
    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization
            WHERE p.transactionDateTime BETWEEN :startDateTime AND :endDateTime
            """)
    Page<BankPayment> findByTransactionDateTimeBetween(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable
    );

    /**
     * Check if payment exists by external transaction ID
     */
    boolean existsByExternalTransactionId(String externalTransactionId);
}
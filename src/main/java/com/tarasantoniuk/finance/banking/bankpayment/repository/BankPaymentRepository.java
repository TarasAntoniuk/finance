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

    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account a
            LEFT JOIN FETCH a.bank b
            LEFT JOIN FETCH b.country
            LEFT JOIN FETCH b.counterparty
            LEFT JOIN FETCH a.currency
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization
            WHERE p.id = :id
            """)
    Optional<BankPayment> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account a
            LEFT JOIN FETCH a.bank b
            LEFT JOIN FETCH b.country
            LEFT JOIN FETCH b.counterparty
            LEFT JOIN FETCH a.currency
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization o
            WHERE (:orgId IS NULL OR o.id = :orgId)
            """)
    Page<BankPayment> findAllWithDetails(@Param("orgId") Long orgId, Pageable pageable);

    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account a
            LEFT JOIN FETCH a.bank b
            LEFT JOIN FETCH b.country
            LEFT JOIN FETCH b.counterparty
            LEFT JOIN FETCH a.currency
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization o
            WHERE p.account.id = :accountId
              AND (:orgId IS NULL OR o.id = :orgId)
            """)
    Page<BankPayment> findByAccountId(@Param("accountId") Long accountId,
                                      @Param("orgId") Long orgId,
                                      Pageable pageable);

    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account a
            LEFT JOIN FETCH a.bank b
            LEFT JOIN FETCH b.country
            LEFT JOIN FETCH b.counterparty
            LEFT JOIN FETCH a.currency
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization o
            WHERE p.counterparty.id = :counterpartyId
              AND (:orgId IS NULL OR o.id = :orgId)
            """)
    Page<BankPayment> findByCounterpartyId(@Param("counterpartyId") Long counterpartyId,
                                           @Param("orgId") Long orgId,
                                           Pageable pageable);

    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account a
            LEFT JOIN FETCH a.bank b
            LEFT JOIN FETCH b.country
            LEFT JOIN FETCH b.counterparty
            LEFT JOIN FETCH a.currency
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization o
            WHERE p.status = :status
              AND (:orgId IS NULL OR o.id = :orgId)
            """)
    Page<BankPayment> findByStatus(@Param("status") DocumentStatus status,
                                   @Param("orgId") Long orgId,
                                   Pageable pageable);

    @Query("""
            SELECT p FROM BankPayment p
            LEFT JOIN FETCH p.account a
            LEFT JOIN FETCH a.bank b
            LEFT JOIN FETCH b.country
            LEFT JOIN FETCH b.counterparty
            LEFT JOIN FETCH a.currency
            LEFT JOIN FETCH p.counterparty
            LEFT JOIN FETCH p.counterpartyBankAccount
            LEFT JOIN FETCH p.currency
            LEFT JOIN FETCH p.organization o
            WHERE p.transactionDateTime BETWEEN :startDateTime AND :endDateTime
              AND (:orgId IS NULL OR o.id = :orgId)
            """)
    Page<BankPayment> findByTransactionDateTimeBetween(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("orgId") Long orgId,
            Pageable pageable
    );

    boolean existsByExternalTransactionId(String externalTransactionId);
}

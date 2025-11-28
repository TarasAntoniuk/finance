package com.tarasantoniuk.finance.banking.bankaccountbalance.repository;

import com.tarasantoniuk.finance.banking.bankaccountbalance.entity.BankAccountBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountBalanceSnapshotRepository extends JpaRepository<BankAccountBalanceSnapshot, Long> {

    /**
     * Find snapshot for specific bank account and date with all relations loaded (N+1 optimization)
     */
    @Query("""
        SELECT s FROM BankAccountBalanceSnapshot s
        LEFT JOIN FETCH s.bankAccount
        LEFT JOIN FETCH s.organization
        LEFT JOIN FETCH s.currency
        WHERE s.bankAccount.id = :bankAccountId
        AND s.snapshotDate = :snapshotDate
        """)
    Optional<BankAccountBalanceSnapshot> findByBankAccountIdAndSnapshotDateWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("snapshotDate") LocalDate snapshotDate
    );

    /**
     * Find latest snapshot for bank account before or on specific date
     */
    @Query("""
        SELECT s FROM BankAccountBalanceSnapshot s
        LEFT JOIN FETCH s.bankAccount
        LEFT JOIN FETCH s.organization
        LEFT JOIN FETCH s.currency
        WHERE s.bankAccount.id = :bankAccountId
        AND s.snapshotDate <= :beforeDate
        ORDER BY s.snapshotDate DESC
        LIMIT 1
        """)
    Optional<BankAccountBalanceSnapshot> findLatestByBankAccountIdBeforeDateWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("beforeDate") LocalDate beforeDate
    );

    /**
     * Find all snapshots for bank account within date range
     */
    @Query("""
        SELECT s FROM BankAccountBalanceSnapshot s
        LEFT JOIN FETCH s.bankAccount
        LEFT JOIN FETCH s.organization
        LEFT JOIN FETCH s.currency
        WHERE s.bankAccount.id = :bankAccountId
        AND s.snapshotDate BETWEEN :startDate AND :endDate
        ORDER BY s.snapshotDate ASC
        """)
    List<BankAccountBalanceSnapshot> findByBankAccountIdAndDateRangeWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find all snapshots for organization within date range
     */
    @Query("""
        SELECT s FROM BankAccountBalanceSnapshot s
        LEFT JOIN FETCH s.bankAccount
        LEFT JOIN FETCH s.organization
        LEFT JOIN FETCH s.currency
        WHERE s.organization.id = :organizationId
        AND s.snapshotDate BETWEEN :startDate AND :endDate
        ORDER BY s.snapshotDate ASC
        """)
    List<BankAccountBalanceSnapshot> findByOrganizationIdAndDateRangeWithRelations(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find all snapshots for bank account
     */
    @Query("""
        SELECT s FROM BankAccountBalanceSnapshot s
        LEFT JOIN FETCH s.bankAccount
        LEFT JOIN FETCH s.organization
        LEFT JOIN FETCH s.currency
        WHERE s.bankAccount.id = :bankAccountId
        ORDER BY s.snapshotDate ASC
        """)
    List<BankAccountBalanceSnapshot> findByBankAccountIdWithRelations(@Param("bankAccountId") Long bankAccountId);

    /**
     * Check if snapshot exists for bank account and date
     */
    boolean existsByBankAccountIdAndSnapshotDate(Long bankAccountId, LocalDate snapshotDate);

    /**
     * Delete all snapshots for bank account after specific date (for recalculation)
     */
    @Query("""
        DELETE FROM BankAccountBalanceSnapshot s
        WHERE s.bankAccount.id = :bankAccountId
        AND s.snapshotDate > :afterDate
        """)
    void deleteByBankAccountIdAfterDate(
            @Param("bankAccountId") Long bankAccountId,
            @Param("afterDate") LocalDate afterDate
    );
}
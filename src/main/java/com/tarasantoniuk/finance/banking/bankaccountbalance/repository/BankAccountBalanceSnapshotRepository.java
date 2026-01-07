package com.tarasantoniuk.finance.banking.bankaccountbalance.repository;

import com.tarasantoniuk.finance.banking.bankaccountbalance.entity.BankAccountBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountBalanceSnapshotRepository extends JpaRepository<BankAccountBalanceSnapshot, Long> {

    /**
     * Find snapshot for specific bank account and datetime with all relations loaded (N+1 optimization)
     */
    @Query("""
            SELECT s FROM BankAccountBalanceSnapshot s
            LEFT JOIN FETCH s.bankAccount
            LEFT JOIN FETCH s.organization
            LEFT JOIN FETCH s.currency
            WHERE s.bankAccount.id = :bankAccountId
            AND s.snapshotDateTime = :snapshotDateTime
            """)
    Optional<BankAccountBalanceSnapshot> findByBankAccountIdAndSnapshotDateTimeWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("snapshotDateTime") LocalDateTime snapshotDateTime
    );

    /**
     * Find latest snapshot for bank account before or at specific datetime
     */
    @Query("""
            SELECT s FROM BankAccountBalanceSnapshot s
            LEFT JOIN FETCH s.bankAccount
            LEFT JOIN FETCH s.organization
            LEFT JOIN FETCH s.currency
            WHERE s.bankAccount.id = :bankAccountId
            AND s.snapshotDateTime <= :beforeDateTime
            ORDER BY s.snapshotDateTime DESC
            LIMIT 1
            """)
    Optional<BankAccountBalanceSnapshot> findLatestByBankAccountIdBeforeDateTimeWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("beforeDateTime") LocalDateTime beforeDateTime
    );

    /**
     * Find all snapshots for bank account within datetime range
     */
    @Query("""
            SELECT s FROM BankAccountBalanceSnapshot s
            LEFT JOIN FETCH s.bankAccount
            LEFT JOIN FETCH s.organization
            LEFT JOIN FETCH s.currency
            WHERE s.bankAccount.id = :bankAccountId
            AND s.snapshotDateTime BETWEEN :startDateTime AND :endDateTime
            ORDER BY s.snapshotDateTime ASC
            """)
    List<BankAccountBalanceSnapshot> findByBankAccountIdAndDateTimeRangeWithRelations(
            @Param("bankAccountId") Long bankAccountId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * Find all snapshots for organization within datetime range
     */
    @Query("""
            SELECT s FROM BankAccountBalanceSnapshot s
            LEFT JOIN FETCH s.bankAccount
            LEFT JOIN FETCH s.organization
            LEFT JOIN FETCH s.currency
            WHERE s.organization.id = :organizationId
            AND s.snapshotDateTime BETWEEN :startDateTime AND :endDateTime
            ORDER BY s.snapshotDateTime ASC
            """)
    List<BankAccountBalanceSnapshot> findByOrganizationIdAndDateTimeRangeWithRelations(
            @Param("organizationId") Long organizationId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
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
            ORDER BY s.snapshotDateTime ASC
            """)
    List<BankAccountBalanceSnapshot> findByBankAccountIdWithRelations(@Param("bankAccountId") Long bankAccountId);

    /**
     * Check if snapshot exists for bank account and datetime
     */
    boolean existsByBankAccountIdAndSnapshotDateTime(Long bankAccountId, LocalDateTime snapshotDateTime);

    /**
     * Delete all snapshots for bank account after specific datetime (for recalculation)
     */
    @Modifying
    @Query("""
            DELETE FROM BankAccountBalanceSnapshot s
            WHERE s.bankAccount.id = :bankAccountId
            AND s.snapshotDateTime > :afterDateTime
            """)
    void deleteByBankAccountIdAfterDateTime(
            @Param("bankAccountId") Long bankAccountId,
            @Param("afterDateTime") LocalDateTime afterDateTime
    );

    /**
     * Delete snapshots for bank account from given datetime (inclusive).
     * Used during recalculation of invalid snapshots.
     *
     * @param bankAccountId bank account ID
     * @param fromDateTime datetime from which to delete snapshots (inclusive)
     * @return count of deleted snapshots
     */
    @Modifying
    @Query("""
            DELETE FROM BankAccountBalanceSnapshot s
            WHERE s.bankAccount.id = :bankAccountId
            AND s.snapshotDateTime >= :fromDateTime
            """)
    int deleteByBankAccountIdAndSnapshotDateTimeGreaterThanEqual(
            @Param("bankAccountId") Long bankAccountId,
            @Param("fromDateTime") LocalDateTime fromDateTime);
}
package com.tarasantoniuk.finance.banking.bankaccount.repository;

import com.tarasantoniuk.finance.banking.bankaccount.entity.BankAccount;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountHolderType;
import com.tarasantoniuk.finance.banking.bankaccount.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    /**
     * Find all bank accounts with bank and currency loaded in a single query.
     * Uses JOIN FETCH to prevent N+1 queries.
     *
     * <p>When {@code orgId} is null (admin), all accounts are returned.
     * Otherwise, returns counterparty accounts (global) and the caller's
     * organization accounts only.
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE (:orgId IS NULL " +
            "   OR ba.holderType = 'COUNTERPARTY' " +
            "   OR (ba.holderType = 'ORGANIZATION' AND ba.holderId = :orgId))")
    List<BankAccount> findAllWithRelations(@Param("orgId") Long orgId);

    /**
     * Find bank account by ID with relationships loaded.
     * Org ownership must be validated by the caller via OrganizationSecurityContext.
     *
     * @param id bank account ID
     * @return optional bank account with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.id = :id")
    Optional<BankAccount> findByIdWithRelations(@Param("id") Long id);

    /**
     * Find bank account by account number with relationships loaded.
     * Org ownership must be validated by the caller via OrganizationSecurityContext.
     *
     * @param accountNumber account number
     * @return optional bank account with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.accountNumber = :accountNumber")
    Optional<BankAccount> findByAccountNumberWithRelations(@Param("accountNumber") String accountNumber);

    /**
     * Find bank accounts by holder with relationships loaded.
     * When {@code orgId} is null (admin) the filter is bypassed.
     *
     * @param holderType holder type
     * @param holderId   holder ID
     * @param orgId      caller's organization (null to bypass for admin)
     * @return list of bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderType = :holderType AND ba.holderId = :holderId " +
            "  AND (:orgId IS NULL " +
            "   OR ba.holderType = 'COUNTERPARTY' " +
            "   OR (ba.holderType = 'ORGANIZATION' AND ba.holderId = :orgId))")
    List<BankAccount> findByHolderWithRelations(
            @Param("holderType") AccountHolderType holderType,
            @Param("holderId") Long holderId,
            @Param("orgId") Long orgId);

    /**
     * Find bank accounts by bank with relationships loaded.
     *
     * @param bankId bank ID
     * @param orgId  caller's organization (null to bypass for admin)
     * @return list of bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.bank.id = :bankId " +
            "  AND (:orgId IS NULL " +
            "   OR ba.holderType = 'COUNTERPARTY' " +
            "   OR (ba.holderType = 'ORGANIZATION' AND ba.holderId = :orgId))")
    List<BankAccount> findByBankIdWithRelations(@Param("bankId") Long bankId,
                                                @Param("orgId") Long orgId);

    /**
     * Find bank accounts by status with relationships loaded.
     *
     * @param status account status
     * @param orgId  caller's organization (null to bypass for admin)
     * @return list of bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.status = :status " +
            "  AND (:orgId IS NULL " +
            "   OR ba.holderType = 'COUNTERPARTY' " +
            "   OR (ba.holderType = 'ORGANIZATION' AND ba.holderId = :orgId))")
    List<BankAccount> findByStatusWithRelations(@Param("status") AccountStatus status,
                                                @Param("orgId") Long orgId);

    /**
     * Find default bank accounts by holder with relationships loaded.
     *
     * @param holderType holder type
     * @param holderId   holder ID
     * @param orgId      caller's organization (null to bypass for admin)
     * @return list of default bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderType = :holderType " +
            "AND ba.holderId = :holderId " +
            "AND ba.isDefault = true " +
            "  AND (:orgId IS NULL " +
            "   OR ba.holderType = 'COUNTERPARTY' " +
            "   OR (ba.holderType = 'ORGANIZATION' AND ba.holderId = :orgId))")
    List<BankAccount> findDefaultByHolderWithRelations(
            @Param("holderType") AccountHolderType holderType,
            @Param("holderId") Long holderId,
            @Param("orgId") Long orgId);

    // Keep original method for duplicate check (doesn't need relationships)
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    /**
     * Find bank accounts by holder ID with relationships loaded.
     * Used for reports to get all accounts for specific organization.
     * Caller must scope {@code holderId} via OrganizationSecurityContext.
     *
     * @param holderId holder ID (organization or counterparty)
     * @return list of bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderId = :holderId")
    List<BankAccount> findByHolderIdWithRelations(@Param("holderId") Long holderId);

    /**
     * Find bank accounts by currency ID with relationships loaded.
     * Used for reports to get all accounts in specific currency.
     *
     * @param currencyId currency ID
     * @return list of bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.currency.id = :currencyId")
    List<BankAccount> findByCurrencyIdWithRelations(@Param("currencyId") Long currencyId);

    /**
     * Find bank accounts by holder ID and currency ID with relationships loaded.
     * Used for reports with multiple filters. Caller must scope {@code holderId}.
     *
     * @param holderId   holder ID (organization or counterparty)
     * @param currencyId currency ID
     * @return list of bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderId = :holderId AND ba.currency.id = :currencyId")
    List<BankAccount> findByHolderIdAndCurrencyIdWithRelations(
            @Param("holderId") Long holderId,
            @Param("currencyId") Long currencyId);

    /**
     * Find all organization bank accounts with relationships loaded.
     * Only returns accounts where holderType = ORGANIZATION.
     *
     * @param orgId caller's organization (null to bypass for admin)
     * @return list of organization bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderType = 'ORGANIZATION' " +
            "  AND (:orgId IS NULL OR ba.holderId = :orgId)")
    List<BankAccount> findOrganizationAccountsWithRelations(@Param("orgId") Long orgId);

    /**
     * Find all counterparty bank accounts with relationships loaded.
     * Counterparty accounts are global (shared directory), no org filtering applied.
     *
     * @return list of counterparty bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderType = 'COUNTERPARTY'")
    List<BankAccount> findCounterpartyAccountsWithRelations();

    /**
     * Find organization bank accounts by holder ID with relationships loaded.
     * Only returns accounts where holderType = ORGANIZATION and holderId matches.
     * Used for reports filtered by specific organization.
     *
     * @param holderId organization ID
     * @return list of organization bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderType = 'ORGANIZATION' AND ba.holderId = :holderId")
    List<BankAccount> findOrganizationAccountsByHolderIdWithRelations(@Param("holderId") Long holderId);

    /**
     * Find organization bank accounts by currency ID with relationships loaded.
     * Only returns accounts where holderType = ORGANIZATION and currency matches.
     * Used for reports filtered by specific currency.
     *
     * @param currencyId currency ID
     * @param orgId      caller's organization (null to bypass for admin)
     * @return list of organization bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderType = 'ORGANIZATION' AND ba.currency.id = :currencyId " +
            "  AND (:orgId IS NULL OR ba.holderId = :orgId)")
    List<BankAccount> findOrganizationAccountsByCurrencyIdWithRelations(
            @Param("currencyId") Long currencyId,
            @Param("orgId") Long orgId);

    /**
     * Find organization bank accounts by holder ID and currency ID with relationships loaded.
     * Only returns accounts where holderType = ORGANIZATION and both holder and currency match.
     * Used for reports with multiple filters.
     *
     * @param holderId   organization ID
     * @param currencyId currency ID
     * @return list of organization bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderType = 'ORGANIZATION' AND ba.holderId = :holderId AND ba.currency.id = :currencyId")
    List<BankAccount> findOrganizationAccountsByHolderIdAndCurrencyIdWithRelations(
            @Param("holderId") Long holderId,
            @Param("currencyId") Long currencyId);

    /**
     * Find all active bank account IDs.
     * Used by scheduler for batch snapshot creation (no user context).
     *
     * @return list of active bank account IDs
     */
    @Query("SELECT ba.id FROM BankAccount ba WHERE ba.status = 'ACTIVE'")
    List<Long> findAllActiveAccountIds();
}

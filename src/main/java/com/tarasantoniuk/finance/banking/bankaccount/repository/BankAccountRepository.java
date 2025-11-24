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
     * @return list of bank accounts with relationships eagerly loaded
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency")
    List<BankAccount> findAllWithRelations();

    /**
     * Find bank account by ID with relationships loaded.
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
     *
     * @param holderType holder type
     * @param holderId holder ID
     * @return list of bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderType = :holderType AND ba.holderId = :holderId")
    List<BankAccount> findByHolderWithRelations(
            @Param("holderType") AccountHolderType holderType,
            @Param("holderId") Long holderId);

    /**
     * Find bank accounts by bank with relationships loaded.
     *
     * @param bankId bank ID
     * @return list of bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.bank.id = :bankId")
    List<BankAccount> findByBankIdWithRelations(@Param("bankId") Long bankId);

    /**
     * Find bank accounts by status with relationships loaded.
     *
     * @param status account status
     * @return list of bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.status = :status")
    List<BankAccount> findByStatusWithRelations(@Param("status") AccountStatus status);

    /**
     * Find default bank accounts by holder with relationships loaded.
     *
     * @param holderType holder type
     * @param holderId holder ID
     * @return list of default bank accounts with relationships
     */
    @Query("SELECT ba FROM BankAccount ba " +
            "LEFT JOIN FETCH ba.bank " +
            "LEFT JOIN FETCH ba.currency " +
            "WHERE ba.holderType = :holderType " +
            "AND ba.holderId = :holderId " +
            "AND ba.isDefault = true")
    List<BankAccount> findDefaultByHolderWithRelations(
            @Param("holderType") AccountHolderType holderType,
            @Param("holderId") Long holderId);

    // Keep original method for duplicate check (doesn't need relationships)
    Optional<BankAccount> findByAccountNumber(String accountNumber);
}
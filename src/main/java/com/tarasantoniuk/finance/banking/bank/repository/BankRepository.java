package com.tarasantoniuk.finance.banking.bank.repository;

import com.tarasantoniuk.finance.banking.bank.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    /**
     * Find all banks with country and counterparty loaded in a single query.
     * Uses JOIN FETCH to prevent N+1 queries.
     *
     * @return list of banks with relationships eagerly loaded
     */
    @Query("SELECT b FROM Bank b " +
            "LEFT JOIN FETCH b.country " +
            "LEFT JOIN FETCH b.counterparty")
    List<Bank> findAllWithRelations();

    /**
     * Find bank by ID with relationships loaded.
     *
     * @param id bank ID
     * @return optional bank with relationships
     */
    @Query("SELECT b FROM Bank b " +
            "LEFT JOIN FETCH b.country " +
            "LEFT JOIN FETCH b.counterparty " +
            "WHERE b.id = :id")
    Optional<Bank> findByIdWithRelations(@Param("id") Long id);

    /**
     * Find banks by country with relationships loaded.
     *
     * @param countryId country ID
     * @return list of banks with relationships
     */
    @Query("SELECT b FROM Bank b " +
            "LEFT JOIN FETCH b.country " +
            "LEFT JOIN FETCH b.counterparty " +
            "WHERE b.country.id = :countryId")
    List<Bank> findByCountryIdWithRelations(@Param("countryId") Long countryId);

    /**
     * Find active banks with relationships loaded.
     *
     * @return list of active banks with relationships
     */
    @Query("SELECT b FROM Bank b " +
            "LEFT JOIN FETCH b.country " +
            "LEFT JOIN FETCH b.counterparty " +
            "WHERE b.isActive = true")
    List<Bank> findActiveWithRelations();

    /**
     * Find bank by SWIFT code with relationships loaded.
     *
     * @param swiftCode SWIFT code
     * @return optional bank with relationships
     */
    @Query("SELECT b FROM Bank b " +
            "LEFT JOIN FETCH b.country " +
            "LEFT JOIN FETCH b.counterparty " +
            "WHERE b.swiftCode = :swiftCode")
    Optional<Bank> findBySwiftCodeWithRelations(@Param("swiftCode") String swiftCode);
}
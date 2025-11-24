package com.tarasantoniuk.finance.core.accountingpolicy.repository;

import com.tarasantoniuk.finance.core.accountingpolicy.entity.AccountingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingPolicyRepository extends JpaRepository<AccountingPolicy, Long> {

    /**
     * Find all accounting policies with organization and currency loaded in a single query.
     * Uses JOIN FETCH to prevent N+1 queries.
     *
     * @return list of accounting policies with relationships eagerly loaded
     */
    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency")
    List<AccountingPolicy> findAllWithRelations();

    /**
     * Find accounting policy by ID with relationships loaded.
     *
     * @param id accounting policy ID
     * @return optional accounting policy with relationships
     */
    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.id = :id")
    Optional<AccountingPolicy> findByIdWithRelations(@Param("id") Long id);

    /**
     * Find accounting policy by organization and year with relationships loaded.
     *
     * @param organizationId organization ID
     * @param year year
     * @return optional accounting policy with relationships
     */
    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.organization.id = :organizationId AND ap.year = :year")
    Optional<AccountingPolicy> findByOrganizationIdAndYearWithRelations(
            @Param("organizationId") Long organizationId,
            @Param("year") Integer year);

    /**
     * Find accounting policies by organization with relationships loaded.
     *
     * @param organizationId organization ID
     * @return list of accounting policies with relationships
     */
    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.organization.id = :organizationId")
    List<AccountingPolicy> findByOrganizationIdWithRelations(@Param("organizationId") Long organizationId);

    /**
     * Find accounting policies by year with relationships loaded.
     *
     * @param year year
     * @return list of accounting policies with relationships
     */
    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.year = :year")
    List<AccountingPolicy> findByYearWithRelations(@Param("year") Integer year);

    /**
     * Find accounting policies by organization and active status with relationships loaded.
     *
     * @param organizationId organization ID
     * @param isActive active status
     * @return list of accounting policies with relationships
     */
    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.organization.id = :organizationId AND ap.isActive = :isActive")
    List<AccountingPolicy> findByOrganizationIdAndIsActiveWithRelations(
            @Param("organizationId") Long organizationId,
            @Param("isActive") Boolean isActive);

    /**
     * Find accounting policies by currency with relationships loaded.
     *
     * @param currencyId currency ID
     * @return list of accounting policies with relationships
     */
    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.currency.id = :currencyId")
    List<AccountingPolicy> findByCurrencyIdWithRelations(@Param("currencyId") Long currencyId);

    /**
     * Find accounting policies by year range with relationships loaded.
     *
     * @param startYear start year
     * @param endYear end year
     * @return list of accounting policies with relationships
     */
    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.year BETWEEN :startYear AND :endYear")
    List<AccountingPolicy> findByYearBetweenWithRelations(
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear);

    // Keep original method for duplicate check (doesn't need relationships)
    boolean existsByOrganizationIdAndYear(Long organizationId, Integer year);
}
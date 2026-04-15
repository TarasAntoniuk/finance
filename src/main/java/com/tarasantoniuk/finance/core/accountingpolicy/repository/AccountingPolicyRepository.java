package com.tarasantoniuk.finance.core.accountingpolicy.repository;

import com.tarasantoniuk.finance.core.accountingpolicy.entity.AccountingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingPolicyRepository extends JpaRepository<AccountingPolicy, Long> {

    @Query(value = "SELECT DISTINCT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization o " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE (:orgId IS NULL OR o.id = :orgId) " +
            "ORDER BY ap.year DESC, ap.id DESC",
            countQuery = "SELECT COUNT(ap) FROM AccountingPolicy ap " +
                    "WHERE (:orgId IS NULL OR ap.organization.id = :orgId)")
    Page<AccountingPolicy> findAllWithRelations(@Param("orgId") Long orgId, Pageable pageable);

    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.id = :id")
    Optional<AccountingPolicy> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.organization.id = :organizationId AND ap.year = :year")
    Optional<AccountingPolicy> findByOrganizationIdAndYearWithRelations(
            @Param("organizationId") Long organizationId,
            @Param("year") Integer year);

    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.organization.id = :organizationId")
    List<AccountingPolicy> findByOrganizationIdWithRelations(@Param("organizationId") Long organizationId, Pageable pageable);

    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization o " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.year = :year " +
            "AND (:orgId IS NULL OR o.id = :orgId)")
    List<AccountingPolicy> findByYearWithRelations(@Param("year") Integer year,
                                                   @Param("orgId") Long orgId,
                                                   Pageable pageable);

    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.organization.id = :organizationId AND ap.isActive = :isActive")
    List<AccountingPolicy> findByOrganizationIdAndIsActiveWithRelations(
            @Param("organizationId") Long organizationId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization o " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.currency.id = :currencyId " +
            "AND (:orgId IS NULL OR o.id = :orgId)")
    List<AccountingPolicy> findByCurrencyIdWithRelations(@Param("currencyId") Long currencyId,
                                                         @Param("orgId") Long orgId,
                                                         Pageable pageable);

    @Query("SELECT ap FROM AccountingPolicy ap " +
            "LEFT JOIN FETCH ap.organization o " +
            "LEFT JOIN FETCH ap.currency " +
            "WHERE ap.year BETWEEN :startYear AND :endYear " +
            "AND (:orgId IS NULL OR o.id = :orgId)")
    List<AccountingPolicy> findByYearBetweenWithRelations(
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear,
            @Param("orgId") Long orgId,
            Pageable pageable);

    boolean existsByOrganizationIdAndYear(Long organizationId, Integer year);
}

package com.tarasantoniuk.finance.core.accountingpolicy.repository;

import com.tarasantoniuk.finance.core.accountingpolicy.entity.AccountingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountingPolicyRepository extends JpaRepository<AccountingPolicy, Long> {

    Optional<AccountingPolicy> findByOrganizationIdAndYear(Long organizationId, Integer year);

    List<AccountingPolicy> findByOrganizationId(Long organizationId);

    List<AccountingPolicy> findByYear(Integer year);

    List<AccountingPolicy> findByOrganizationIdAndIsActive(Long organizationId, Boolean isActive);

    List<AccountingPolicy> findByCurrencyId(Long currencyId);

    boolean existsByOrganizationIdAndYear(Long organizationId, Integer year);

    List<AccountingPolicy> findByYearBetween(Integer startYear, Integer endYear);

    Optional<AccountingPolicy> findByOrganizationIdAndYearAndIsActive(Long organizationId, Integer year, Boolean isActive);
}
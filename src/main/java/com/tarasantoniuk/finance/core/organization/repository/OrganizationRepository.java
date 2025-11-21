package com.tarasantoniuk.finance.core.organization.repository;

import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByCountryId(Long countryId);

    Optional<Organization> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Organization> findByNameContainingIgnoreCase(String name);
}
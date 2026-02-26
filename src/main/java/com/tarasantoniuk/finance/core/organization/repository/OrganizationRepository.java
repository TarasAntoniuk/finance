package com.tarasantoniuk.finance.core.organization.repository;

import com.tarasantoniuk.finance.core.organization.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * Find all organizations with country loaded in a single query.
     * Uses JOIN FETCH to prevent N+1 queries.
     *
     * @return list of organizations with country eagerly loaded
     */
    @Query(value = "SELECT DISTINCT o FROM Organization o " +
            "LEFT JOIN FETCH o.country " +
            "ORDER BY o.name ASC",
            countQuery = "SELECT COUNT(o) FROM Organization o")
    Page<Organization> findAllWithCountry(Pageable pageable);

    /**
     * Find organization by ID with country loaded.
     *
     * @param id organization ID
     * @return optional organization with country
     */
    @Query("SELECT o FROM Organization o " +
            "LEFT JOIN FETCH o.country " +
            "WHERE o.id = :id")
    Optional<Organization> findByIdWithCountry(@Param("id") Long id);

    /**
     * Find organizations by country with country loaded.
     *
     * @param countryId country ID
     * @return list of organizations with country
     */
    @Query("SELECT o FROM Organization o " +
            "LEFT JOIN FETCH o.country " +
            "WHERE o.country.id = :countryId")
    List<Organization> findByCountryIdWithCountry(@Param("countryId") Long countryId, Pageable pageable);

    /**
     * Search organizations by name with country loaded.
     *
     * @param name search term (case insensitive)
     * @return list of organizations with country
     */
    @Query(value = "SELECT DISTINCT o FROM Organization o " +
            "LEFT JOIN FETCH o.country " +
            "WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "ORDER BY o.name ASC",
            countQuery = "SELECT COUNT(o) FROM Organization o " +
                    "WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Organization> findByNameContainingIgnoreCaseWithCountry(@Param("name") String name, Pageable pageable);

    // Keep original methods for duplicate checks (don't need country)
    Optional<Organization> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);
}
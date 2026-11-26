package com.tarasantoniuk.finance.core.item.repository;

import com.tarasantoniuk.finance.core.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    boolean existsByCode(String code);

    /**
     * Case-insensitive search by code or name; an empty {@code text} matches every item.
     */
    @Query("""
            SELECT i FROM Item i
            WHERE (LOWER(i.code) LIKE LOWER(CONCAT('%', :text, '%'))
                   OR LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')))
              AND (:activeOnly = false OR i.isActive = true)
            """)
    Page<Item> search(@Param("text") String text, @Param("activeOnly") boolean activeOnly, Pageable pageable);
}

package com.tarasantoniuk.finance.security.audit.repository;

import com.tarasantoniuk.finance.security.audit.entity.SecurityAuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityAuditEntryRepository extends JpaRepository<SecurityAuditEntry, Long> {
}

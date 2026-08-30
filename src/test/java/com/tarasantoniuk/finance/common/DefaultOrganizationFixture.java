package com.tarasantoniuk.finance.common;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Seeds the organization that {@code app.provisioning.default-organization-id} points at.
 * Every new account is attached to it, so registration fails without it.
 *
 * <p>Inserted through JDBC with an explicit id because the entities use
 * {@code GenerationType.IDENTITY} and the configured id must match exactly.
 */
public final class DefaultOrganizationFixture {

    private static final long COUNTRY_ID = 9001L;
    private static final String COUNTRY_NAME = "Testland";
    private static final String COUNTRY_ISO_CODE = "TST";
    private static final String ORGANIZATION_NAME = "Default Test Organization";

    private DefaultOrganizationFixture() {
    }

    public static void ensureExists(JdbcTemplate jdbcTemplate, long organizationId) {
        Integer existing = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM organizations WHERE id = ?", Integer.class, organizationId);
        if (existing != null && existing > 0) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO countries (id, name, iso_code, created_at, updated_at)
                VALUES (?, ?, ?, now(), now())
                ON CONFLICT (id) DO NOTHING
                """, COUNTRY_ID, COUNTRY_NAME, COUNTRY_ISO_CODE);

        jdbcTemplate.update("""
                INSERT INTO organizations (id, name, country_id, created_at, updated_at)
                VALUES (?, ?, ?, now(), now())
                """, organizationId, ORGANIZATION_NAME, COUNTRY_ID);
    }
}

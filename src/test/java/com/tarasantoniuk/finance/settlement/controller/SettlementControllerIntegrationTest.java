package com.tarasantoniuk.finance.settlement.controller;

import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.security.jwt.service.JwtService;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.settlement.model.DocumentRef;
import com.tarasantoniuk.finance.settlement.model.SettlementKey;
import com.tarasantoniuk.finance.settlement.service.SettlementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Settlement reads go through the JWT filter and are scoped to the caller's organization.
 */
@AutoConfigureMockMvc
class SettlementControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private SettlementService settlementService;
    @Autowired private TestDataFactoryCore factoryCore;
    @Autowired private TestDataCleanerCore cleanerCore;

    private Organization ownOrganization;
    private Organization otherOrganization;
    private Counterparty supplier;

    @BeforeEach
    void setUp() {
        cleanerCore.cleanAll();
        Country country = factoryCore.createUkraine();
        Currency currency = factoryCore.createUAH();
        ownOrganization = factoryCore.createOrganization("Own LLC", "UA100", country);
        otherOrganization = factoryCore.createOrganization("Other LLC", "UA200", country);
        supplier = factoryCore.createSupplierCounterparty();
        invoice(ownOrganization, currency, 1L, "1000.00");
        invoice(otherOrganization, currency, 2L, "250.00");
    }

    @AfterEach
    void tearDown() {
        cleanerCore.cleanAll();
    }

    @Test
    void payables_AsUser_ShouldShowOnlyOwnOrganization() throws Exception {
        perform(get("/api/settlements/payables"), UserRole.USER, ownOrganization)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].organizationId").value(ownOrganization.getId()))
                .andExpect(jsonPath("$[0].balance").value(1000.0));
    }

    @Test
    void payables_AsUserForAnotherOrganization_ShouldReturn403() throws Exception {
        perform(get("/api/settlements/payables").param("organizationId", otherOrganization.getId().toString()),
                UserRole.USER, ownOrganization)
                .andExpect(status().isForbidden());
    }

    @Test
    void payables_AsAdmin_ShouldShowAllOrganizations() throws Exception {
        perform(get("/api/settlements/payables"), UserRole.ADMIN, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void openItems_AsGuestOfOwnOrganization_ShouldListOwnInvoicesOnly() throws Exception {
        perform(get("/api/settlements/counterparties/" + supplier.getId() + "/open-items"), UserRole.GUEST, ownOrganization)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].documentId").value(1))
                .andExpect(jsonPath("$[0].overdue").value(false));
    }

    @Test
    void payables_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/settlements/payables")).andExpect(status().isUnauthorized());
    }

    private void invoice(Organization organization, Currency currency, long id, String amount) {
        settlementService.postInvoice(new SettlementKey(organization.getId(), supplier.getId(), currency.getId()),
                new DocumentRef("PurchaseInvoice", id), LocalDateTime.now(), LocalDate.now().plusDays(30),
                new BigDecimal(amount));
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, UserRole role, Organization organization)
            throws Exception {
        User user = new User();
        user.setId(role.ordinal() + 910_000L);
        user.setEmail(role.name().toLowerCase() + "@settlements.test");
        user.setRole(role);
        user.setOrganization(organization);
        return mockMvc.perform(request.header("Authorization", "Bearer " + jwtService.generateAccessToken(user)));
    }
}

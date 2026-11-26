package com.tarasantoniuk.finance.purchase.purchaseinvoice.controller;

import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.common.TestDataCleanerCore;
import com.tarasantoniuk.finance.core.common.TestDataFactoryCore;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.country.entity.Country;
import com.tarasantoniuk.finance.core.currency.entity.Currency;
import com.tarasantoniuk.finance.core.item.entity.Item;
import com.tarasantoniuk.finance.core.item.enums.ItemType;
import com.tarasantoniuk.finance.core.item.enums.VatCategory;
import com.tarasantoniuk.finance.core.item.repository.ItemRepository;
import com.tarasantoniuk.finance.core.organization.entity.Organization;
import com.tarasantoniuk.finance.security.jwt.service.JwtService;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.settlement.model.DocumentRef;
import com.tarasantoniuk.finance.settlement.model.SettlementKey;
import com.tarasantoniuk.finance.settlement.service.SettlementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full stack against PostgreSQL: JWT filter, authorization rules, organization scoping,
 * validation, totals and the settlement side effects of posting and unposting.
 */
@AutoConfigureMockMvc
@DisplayName("Purchase invoices")
class PurchaseInvoiceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private SettlementService settlementService;
    @Autowired private TestDataFactoryCore factoryCore;
    @Autowired private TestDataCleanerCore cleanerCore;

    private Organization organization;
    private Organization otherOrganization;
    private Counterparty supplier;
    private Counterparty customer;
    private Currency currency;
    private Item consulting;
    private Item licence;

    @BeforeEach
    void setUp() {
        cleanerCore.cleanAll();
        Country country = factoryCore.createUkraine();
        currency = factoryCore.createUAH();
        organization = factoryCore.createOrganization("Own LLC", "UA100", country);
        otherOrganization = factoryCore.createOrganization("Other LLC", "UA200", country);
        supplier = factoryCore.createSupplierCounterparty();
        customer = factoryCore.createCustomerCounterparty();
        consulting = item("CONS-H", "HUR", VatCategory.S, "20.00");
        licence = item("LIC-EXEMPT", "ANN", VatCategory.E, "0.00");
    }

    @AfterEach
    void tearDown() {
        cleanerCore.cleanAll();
    }

    @Nested
    @DisplayName("Creating and editing drafts")
    class Drafts {

        @Test
        void create_AsUser_ShouldComputeTotalsAndUseItemDefaults() throws Exception {
            asUser(post("/api/purchase-invoices").contentType(MediaType.APPLICATION_JSON).content(invoiceJson("INV-1",
                    line(consulting, "16", "45.00"), line(licence, "1", "300.00"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.organizationId").value(organization.getId()))
                    .andExpect(jsonPath("$.lines[0].unitCode").value("HUR"))
                    .andExpect(jsonPath("$.lines[0].vatCategory").value("S"))
                    .andExpect(jsonPath("$.lines[0].lineNet").value(720.0))
                    .andExpect(jsonPath("$.totalNet").value(1020.0))
                    .andExpect(jsonPath("$.totalVat").value(144.0))
                    .andExpect(jsonPath("$.totalGross").value(1164.0))
                    .andExpect(jsonPath("$.vatBreakdown.length()").value(2));
        }

        @Test
        void create_AsGuest_ShouldReturn403() throws Exception {
            perform(post("/api/purchase-invoices").contentType(MediaType.APPLICATION_JSON)
                    .content(invoiceJson("INV-1", line(consulting, "1", "10.00"))), UserRole.GUEST, organization)
                    .andExpect(status().isForbidden());
        }

        @Test
        void create_WithSameSupplierNumberTwice_ShouldReturn409() throws Exception {
            createDraft("INV-1", line(consulting, "1", "10.00"));

            asUser(post("/api/purchase-invoices").contentType(MediaType.APPLICATION_JSON)
                    .content(invoiceJson("INV-1", line(consulting, "1", "10.00"))))
                    .andExpect(status().isConflict());
        }

        @Test
        void create_WithCustomerAsSupplier_ShouldReturn400() throws Exception {
            String body = invoiceJson("INV-1", line(consulting, "1", "10.00"))
                    .replace("\"supplierId\":" + supplier.getId(), "\"supplierId\":" + customer.getId());

            asUser(post("/api/purchase-invoices").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("is a customer")));
        }

        @Test
        void create_WithDueDateBeforeIssueDate_ShouldReturn400() throws Exception {
            String body = invoiceJson("INV-1", line(consulting, "1", "10.00"))
                    .replace("\"dueDate\":\"" + LocalDate.now().plusDays(30), "\"dueDate\":\"" + LocalDate.now().minusDays(1));

            asUser(post("/api/purchase-invoices").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("before the issue date")));
        }

        @Test
        void create_WithRateContradictingCategory_ShouldReturn400() throws Exception {
            String badLine = "{\"itemId\":" + consulting.getId()
                    + ",\"quantity\":1,\"unitPriceNet\":10,\"vatCategory\":\"E\",\"vatRate\":20}";

            asUser(post("/api/purchase-invoices").contentType(MediaType.APPLICATION_JSON)
                    .content(invoiceJson("INV-1", badLine)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Line 1: VAT category E")));
        }

        @Test
        void create_WithoutLines_ShouldReturn400() throws Exception {
            asUser(post("/api/purchase-invoices").contentType(MediaType.APPLICATION_JSON).content(invoiceJson("INV-1")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors[0].field").value("lines"));
        }

        @Test
        void update_Draft_ShouldReplaceLines() throws Exception {
            long id = createDraft("INV-1", line(consulting, "1", "10.00"), line(licence, "1", "5.00"));

            asUser(put("/api/purchase-invoices/" + id).contentType(MediaType.APPLICATION_JSON)
                    .content(invoiceJson("INV-1", line(consulting, "2", "50.00"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.lines.length()").value(1))
                    .andExpect(jsonPath("$.totalGross").value(120.0));
        }

        @Test
        void getById_FromAnotherOrganization_ShouldReturn403() throws Exception {
            long id = createDraft("INV-1", line(consulting, "1", "10.00"));

            perform(get("/api/purchase-invoices/" + id), UserRole.USER, otherOrganization)
                    .andExpect(status().isForbidden());
        }

        @Test
        void search_ShouldListOnlyOwnOrganization() throws Exception {
            createDraft("INV-1", line(consulting, "1", "10.00"));

            asUser(get("/api/purchase-invoices"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].supplierInvoiceNumber").value("INV-1"));
            perform(get("/api/purchase-invoices"), UserRole.USER, otherOrganization)
                    .andExpect(jsonPath("$.content.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Posting and settlements")
    class Posting {

        @Test
        void post_ShouldOpenTheDebtToTheSupplier() throws Exception {
            long id = createDraft("INV-1", line(consulting, "10", "100.00"));

            asUser(post("/api/purchase-invoices/" + id + "/post"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("POSTED"));

            asUser(get("/api/settlements/payables"))
                    .andExpect(jsonPath("$[0].counterpartyId").value(supplier.getId()))
                    .andExpect(jsonPath("$[0].balance").value(1200.0));
            asUser(get("/api/settlements/counterparties/" + supplier.getId() + "/open-items"))
                    .andExpect(jsonPath("$[0].documentType").value("PurchaseInvoice"))
                    .andExpect(jsonPath("$[0].documentId").value(id))
                    .andExpect(jsonPath("$[0].dueDate").value(LocalDate.now().plusDays(30).toString()));
        }

        @Test
        void post_ShouldConsumeAnEarlierPrepayment() throws Exception {
            settlementService.postPayment(key(), new DocumentRef("BankPayment", 1L), LocalDateTime.now(),
                    new BigDecimal("500.00"), List.of());
            long id = createDraft("INV-1", line(consulting, "1", "250.00"));

            asUser(post("/api/purchase-invoices/" + id + "/post")).andExpect(status().isOk());

            asUser(get("/api/settlements/counterparties/" + supplier.getId() + "/open-items"))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].documentType").value("BankPayment"))
                    .andExpect(jsonPath("$[0].openAmount").value(-200.0));
        }

        @Test
        void postTwice_ShouldReturn409() throws Exception {
            long id = createDraft("INV-1", line(consulting, "1", "10.00"));
            asUser(post("/api/purchase-invoices/" + id + "/post")).andExpect(status().isOk());

            asUser(post("/api/purchase-invoices/" + id + "/post")).andExpect(status().isConflict());
        }

        @Test
        void post_WithZeroTotal_ShouldReturn400() throws Exception {
            long id = createDraft("INV-1", line(consulting, "1", "0.00"));

            asUser(post("/api/purchase-invoices/" + id + "/post"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("must be positive")));
        }

        @Test
        void unpost_ShouldRemoveTheDebt() throws Exception {
            long id = createDraft("INV-1", line(consulting, "1", "100.00"));
            asUser(post("/api/purchase-invoices/" + id + "/post")).andExpect(status().isOk());

            asUser(post("/api/purchase-invoices/" + id + "/unpost"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DRAFT"));

            asUser(get("/api/settlements/payables")).andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void unpost_WithPaymentAllocated_ShouldReturn409AndStayPosted() throws Exception {
            long id = createDraft("INV-1", line(consulting, "1", "100.00"));
            asUser(post("/api/purchase-invoices/" + id + "/post")).andExpect(status().isOk());
            settlementService.postPayment(key(), new DocumentRef("BankPayment", 1L), LocalDateTime.now(),
                    new BigDecimal("50.00"), List.of());

            asUser(post("/api/purchase-invoices/" + id + "/unpost"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(containsString("reverse those documents first")));
            asUser(get("/api/purchase-invoices/" + id)).andExpect(jsonPath("$.status").value("POSTED"));
        }

        @Test
        void editingOrDeletingPostedInvoice_ShouldReturn409() throws Exception {
            long id = createDraft("INV-1", line(consulting, "1", "100.00"));
            asUser(post("/api/purchase-invoices/" + id + "/post")).andExpect(status().isOk());

            asUser(put("/api/purchase-invoices/" + id).contentType(MediaType.APPLICATION_JSON)
                    .content(invoiceJson("INV-1", line(consulting, "2", "100.00"))))
                    .andExpect(status().isConflict());
            perform(delete("/api/purchase-invoices/" + id), UserRole.ADMIN, null)
                    .andExpect(status().isConflict());
        }

        @Test
        void deleteDraft_AsUser_ShouldReturn403_AndAsAdmin_ShouldReturn204() throws Exception {
            long id = createDraft("INV-1", line(consulting, "1", "100.00"));

            asUser(delete("/api/purchase-invoices/" + id)).andExpect(status().isForbidden());
            perform(delete("/api/purchase-invoices/" + id), UserRole.ADMIN, null).andExpect(status().isNoContent());
        }
    }

    private long createDraft(String number, String... lines) throws Exception {
        String response = asUser(post("/api/purchase-invoices").contentType(MediaType.APPLICATION_JSON)
                .content(invoiceJson(number, lines)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(response.replaceFirst("^\\{\"id\":(\\d+).*", "$1").replaceAll("(?s)[^0-9].*", ""));
    }

    private String invoiceJson(String number, String... lines) {
        return """
                {"supplierId":%d,"supplierInvoiceNumber":"%s","issueDate":"%s","dueDate":"%s",
                 "currencyId":%d,"lines":[%s]}
                """.formatted(supplier.getId(), number, LocalDate.now(), LocalDate.now().plusDays(30),
                currency.getId(), String.join(",", lines));
    }

    private static String line(Item item, String quantity, String price) {
        return "{\"itemId\":%d,\"quantity\":%s,\"unitPriceNet\":%s}".formatted(item.getId(), quantity, price);
    }

    private Item item(String code, String unitCode, VatCategory category, String rate) {
        Item item = new Item();
        item.setCode(code);
        item.setName(code);
        item.setType(ItemType.SERVICE);
        item.setUnitCode(unitCode);
        item.setVatCategory(category);
        item.setVatRate(new BigDecimal(rate));
        return itemRepository.save(item);
    }

    private SettlementKey key() {
        return new SettlementKey(organization.getId(), supplier.getId(), currency.getId());
    }

    private ResultActions asUser(MockHttpServletRequestBuilder request) throws Exception {
        return perform(request, UserRole.USER, organization);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, UserRole role, Organization userOrganization)
            throws Exception {
        User user = new User();
        user.setId(role.ordinal() + 920_000L);
        user.setEmail(role.name().toLowerCase() + "@invoices.test");
        user.setRole(role);
        user.setOrganization(userOrganization);
        return mockMvc.perform(request.header("Authorization", "Bearer " + jwtService.generateAccessToken(user)));
    }
}

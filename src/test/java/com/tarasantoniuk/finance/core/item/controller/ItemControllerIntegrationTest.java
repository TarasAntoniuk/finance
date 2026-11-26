package com.tarasantoniuk.finance.core.item.controller;

import com.tarasantoniuk.finance.common.BaseIntegrationTest;
import com.tarasantoniuk.finance.core.item.repository.ItemRepository;
import com.tarasantoniuk.finance.security.jwt.service.JwtService;
import com.tarasantoniuk.finance.security.user.entity.User;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full stack against PostgreSQL: JWT filter, authorization rules, validation, persistence.
 */
@AutoConfigureMockMvc
class ItemControllerIntegrationTest extends BaseIntegrationTest {

    private static final String HOURLY_CONSULTING = """
            {"code":"CONS-H","name":"Senior consultant, hourly","type":"SERVICE",
             "unitCode":"HUR","vatCategory":"S","vatRate":20.00}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    @AfterEach
    void cleanItems() {
        itemRepository.deleteAll();
    }

    @Test
    void create_AsUser_ShouldReturn201() throws Exception {
        perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(HOURLY_CONSULTING), UserRole.USER)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.unitCode").value("HUR"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void create_AsGuest_ShouldReturn403() throws Exception {
        perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(HOURLY_CONSULTING), UserRole.GUEST)
                .andExpect(status().isForbidden());
    }

    @Test
    void create_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(HOURLY_CONSULTING))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_WithDuplicateCode_ShouldReturn409() throws Exception {
        createAsUser(HOURLY_CONSULTING);

        perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(HOURLY_CONSULTING), UserRole.USER)
                .andExpect(status().isConflict());
    }

    @Test
    void create_WithZeroRateForStandardCategory_ShouldReturn400() throws Exception {
        String body = HOURLY_CONSULTING.replace("20.00", "0.00");

        perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(body), UserRole.USER)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("VAT category S")));
    }

    @Test
    void create_WithInvalidUnitCode_ShouldReturn400() throws Exception {
        String body = HOURLY_CONSULTING.replace("\"HUR\"", "\"hours\"");

        perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(body), UserRole.USER)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("unitCode"));
    }

    @Test
    void search_AsGuest_ShouldFindByCodeOrNameIgnoringCase() throws Exception {
        createAsUser(HOURLY_CONSULTING);
        createAsUser("""
                {"code":"LIC-ANNUAL","name":"Software licence, annual","type":"SERVICE",
                 "unitCode":"ANN","vatCategory":"S","vatRate":20.00}
                """);

        perform(get("/api/items").param("q", "consult"), UserRole.GUEST)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].code").value("CONS-H"))
                .andExpect(jsonPath("$.metadata.totalElements").value(1));
        perform(get("/api/items").param("q", "lic-"), UserRole.GUEST)
                .andExpect(jsonPath("$.content[0].code").value("LIC-ANNUAL"));
    }

    @Test
    void search_ActiveOnly_ShouldSkipDeactivatedItems() throws Exception {
        long id = createAsUser(HOURLY_CONSULTING);
        perform(patch("/api/items/" + id + "/deactivate"), UserRole.USER).andExpect(status().isOk());

        perform(get("/api/items").param("activeOnly", "true"), UserRole.USER)
                .andExpect(jsonPath("$.content.length()").value(0));
        perform(get("/api/items"), UserRole.USER)
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void update_AsUser_ShouldChangeVat() throws Exception {
        long id = createAsUser(HOURLY_CONSULTING);
        String exempt = HOURLY_CONSULTING.replace("\"S\"", "\"E\"").replace("20.00", "0.00");

        perform(put("/api/items/" + id).contentType(MediaType.APPLICATION_JSON).content(exempt), UserRole.USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vatCategory").value("E"))
                .andExpect(jsonPath("$.vatRate").value(0.0));
    }

    @Test
    void delete_AsUser_ShouldReturn403_AndAsAdmin_ShouldReturn204() throws Exception {
        long id = createAsUser(HOURLY_CONSULTING);

        perform(delete("/api/items/" + id), UserRole.USER).andExpect(status().isForbidden());
        perform(delete("/api/items/" + id), UserRole.ADMIN).andExpect(status().isNoContent());
        perform(get("/api/items/" + id), UserRole.ADMIN).andExpect(status().isNotFound());
    }

    private long createAsUser(String body) throws Exception {
        String response = perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(body), UserRole.USER)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(response.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, UserRole role) throws Exception {
        return mockMvc.perform(request.header("Authorization", "Bearer " + tokenFor(role)));
    }

    private String tokenFor(UserRole role) {
        User user = new User();
        user.setId(role.ordinal() + 900_000L);
        user.setEmail(role.name().toLowerCase() + "@items.test");
        user.setRole(role);
        return jwtService.generateAccessToken(user);
    }
}

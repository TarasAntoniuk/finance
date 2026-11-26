package com.tarasantoniuk.finance.common;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private static final String INTERNAL_DETAIL = "connection to db-internal-host:5432 refused";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void malformedJsonBody_ShouldReturn400WithoutParserDetails() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.MALFORMED_BODY_MESSAGE))
                .andExpect(jsonPath("$.path").value("/test/body"));
    }

    @Test
    void unsupportedContentType_ShouldReturn415() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void responseStatusException_ShouldKeepItsStatusAndReason() throws Exception {
        mockMvc.perform(get("/test/gone"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("Document archived"));
    }

    @Test
    void dataIntegrityViolation_ShouldReturn409WithGenericMessage() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.DATA_CONFLICT_MESSAGE))
                .andExpect(content().string(not(containsString(INTERNAL_DETAIL))));
    }

    @Test
    void unexpectedException_ShouldReturn500WithoutInternalMessage() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.UNEXPECTED_ERROR_MESSAGE))
                .andExpect(content().string(not(containsString(INTERNAL_DETAIL))));
    }

    @Test
    void servletExceptionWithoutStatus_ShouldBeTreatedAsUnexpected() throws Exception {
        mockMvc.perform(get("/test/servlet"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(GlobalExceptionHandler.UNEXPECTED_ERROR_MESSAGE))
                .andExpect(content().string(not(containsString(INTERNAL_DETAIL))));
    }

    record NamePayload(String name) {
    }

    @RestController
    static class FailingController {

        @PostMapping("/test/body")
        NamePayload echo(@RequestBody NamePayload payload) {
            return payload;
        }

        @GetMapping("/test/gone")
        void gone() {
            throw new ResponseStatusException(HttpStatus.GONE, "Document archived");
        }

        @GetMapping("/test/conflict")
        void conflict() {
            throw new DataIntegrityViolationException(INTERNAL_DETAIL);
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException(INTERNAL_DETAIL);
        }

        @GetMapping("/test/servlet")
        void servlet() throws ServletException {
            throw new ServletException(INTERNAL_DETAIL);
        }
    }
}

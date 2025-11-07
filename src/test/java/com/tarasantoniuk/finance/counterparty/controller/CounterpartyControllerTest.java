package com.tarasantoniuk.finance.counterparty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.counterparty.exception.CounterpartyNotFoundException;
import com.tarasantoniuk.finance.counterparty.exception.DuplicateCounterpartyException;
import com.tarasantoniuk.finance.counterparty.service.CounterpartyService;
import com.tarasantoniuk.finance.country.exception.CountryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CounterpartyController.class)
class CounterpartyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CounterpartyService counterpartyService;

    private CounterpartyRequestDto requestDto;
    private CounterpartyResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new CounterpartyRequestDto();
        requestDto.setCode("CP001");
        requestDto.setName("Test Counterparty");
        requestDto.setType(Counterparty.CounterpartyType.CUSTOMER);
        requestDto.setCountryId(1L);

        responseDto = new CounterpartyResponseDto();
        responseDto.setId(1L);
        responseDto.setCode("CP001");
        responseDto.setName("Test Counterparty");
        responseDto.setIsActive(true);
    }

    // ========== CREATE TESTS ==========

    @Test
    void create_WhenValid_ShouldReturnCreated() throws Exception {
        // Given
        when(counterpartyService.create(any(CounterpartyRequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/counterparties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("CP001"))
                .andExpect(jsonPath("$.name").value("Test Counterparty"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(counterpartyService, times(1)).create(any(CounterpartyRequestDto.class));
    }

    @Test
    void create_WhenCodeExists_ShouldReturnConflict() throws Exception {
        // Given
        when(counterpartyService.create(any(CounterpartyRequestDto.class)))
                .thenThrow(new DuplicateCounterpartyException("Counterparty with code CP001 already exists"));

        // When & Then
        mockMvc.perform(post("/api/counterparties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());

        verify(counterpartyService, times(1)).create(any(CounterpartyRequestDto.class));
    }

    @Test
    void create_WhenCountryNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(counterpartyService.create(any(CounterpartyRequestDto.class)))
                .thenThrow(new CountryNotFoundException(1L));

        // When & Then
        mockMvc.perform(post("/api/counterparties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        verify(counterpartyService, times(1)).create(any(CounterpartyRequestDto.class));
    }

    @Test
    void create_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        requestDto.setCode(null); // Required field is null

        // When & Then
        mockMvc.perform(post("/api/counterparties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(counterpartyService, never()).create(any(CounterpartyRequestDto.class));
    }

    // ========== GET BY ID TESTS ==========

    @Test
    void getById_WhenExists_ShouldReturnCounterparty() throws Exception {
        // Given
        when(counterpartyService.getById(1L)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/counterparties/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.code").value("CP001"))
                .andExpect(jsonPath("$.name").value("Test Counterparty"));

        verify(counterpartyService, times(1)).getById(1L);
    }

    @Test
    void getById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(counterpartyService.getById(1L))
                .thenThrow(CounterpartyNotFoundException.byId(1L));

        // When & Then
        mockMvc.perform(get("/api/counterparties/1"))
                .andExpect(status().isNotFound());

        verify(counterpartyService, times(1)).getById(1L);
    }

    // ========== GET ALL TESTS ==========

    @Test
    void getAll_ShouldReturnAllCounterparties() throws Exception {
        // Given
        CounterpartyResponseDto responseDto2 = new CounterpartyResponseDto();
        responseDto2.setId(2L);
        responseDto2.setCode("CP002");
        responseDto2.setName("Second Counterparty");

        when(counterpartyService.getAll()).thenReturn(List.of(responseDto, responseDto2));

        // When & Then
        mockMvc.perform(get("/api/counterparties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].code").value("CP001"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].code").value("CP002"));

        verify(counterpartyService, times(1)).getAll();
    }

    @Test
    void getAll_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(counterpartyService.getAll()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/counterparties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(counterpartyService, times(1)).getAll();
    }

    // ========== UPDATE TESTS ==========

    @Test
    void update_WhenValid_ShouldReturnUpdatedCounterparty() throws Exception {
        // Given
        responseDto.setName("Updated Counterparty");
        when(counterpartyService.update(eq(1L), any(CounterpartyRequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(put("/api/counterparties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Counterparty"));

        verify(counterpartyService, times(1)).update(eq(1L), any(CounterpartyRequestDto.class));
    }

    @Test
    void update_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(counterpartyService.update(eq(1L), any(CounterpartyRequestDto.class)))
                .thenThrow(CounterpartyNotFoundException.byId(1L));

        // When & Then
        mockMvc.perform(put("/api/counterparties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        verify(counterpartyService, times(1)).update(eq(1L), any(CounterpartyRequestDto.class));
    }

    @Test
    void update_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        requestDto.setCode(null);

        // When & Then
        mockMvc.perform(put("/api/counterparties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(counterpartyService, never()).update(anyLong(), any(CounterpartyRequestDto.class));
    }

    @Test
    void update_WhenChangingToExistingCode_ShouldReturnConflict() throws Exception {
        // Given
        when(counterpartyService.update(eq(1L), any(CounterpartyRequestDto.class)))
                .thenThrow(new DuplicateCounterpartyException("Counterparty with code CP002 already exists"));

        // When & Then
        mockMvc.perform(put("/api/counterparties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());

        verify(counterpartyService, times(1)).update(eq(1L), any(CounterpartyRequestDto.class));
    }

    // ========== DELETE TESTS ==========

    @Test
    void delete_WhenExists_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(counterpartyService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/counterparties/1"))
                .andExpect(status().isNoContent());

        verify(counterpartyService, times(1)).delete(1L);
    }

    @Test
    void delete_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(CounterpartyNotFoundException.byId(1L)).when(counterpartyService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/counterparties/1"))
                .andExpect(status().isNotFound());

        verify(counterpartyService, times(1)).delete(1L);
    }

    // ========== ACTIVATE TESTS ==========

    @Test
    void activate_WhenExists_ShouldReturnOk() throws Exception {
        // Given
        doNothing().when(counterpartyService).activate(1L);

        // When & Then
        mockMvc.perform(patch("/api/counterparties/1/activate"))
                .andExpect(status().isOk());

        verify(counterpartyService, times(1)).activate(1L);
    }

    @Test
    void activate_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(CounterpartyNotFoundException.byId(1L)).when(counterpartyService).activate(1L);

        // When & Then
        mockMvc.perform(patch("/api/counterparties/1/activate"))
                .andExpect(status().isNotFound());

        verify(counterpartyService, times(1)).activate(1L);
    }

    // ========== DEACTIVATE TESTS ==========

    @Test
    void deactivate_WhenExists_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(counterpartyService).deactivate(1L);

        // When & Then
        mockMvc.perform(patch("/api/counterparties/1/deactivate"))
                .andExpect(status().isNoContent());

        verify(counterpartyService, times(1)).deactivate(1L);
    }

    @Test
    void deactivate_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(CounterpartyNotFoundException.byId(1L)).when(counterpartyService).deactivate(1L);

        // When & Then
        mockMvc.perform(patch("/api/counterparties/1/deactivate"))
                .andExpect(status().isNotFound());

        verify(counterpartyService, times(1)).deactivate(1L);
    }
}
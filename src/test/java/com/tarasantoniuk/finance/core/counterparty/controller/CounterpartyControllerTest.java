package com.tarasantoniuk.finance.core.counterparty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.common.dto.PageMetadata;
import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyRequestDto;
import com.tarasantoniuk.finance.core.counterparty.dto.CounterpartyResponseDto;
import com.tarasantoniuk.finance.core.counterparty.entity.Counterparty;
import com.tarasantoniuk.finance.core.counterparty.exception.CounterpartyNotFoundException;
import com.tarasantoniuk.finance.core.counterparty.exception.DuplicateCounterpartyException;
import com.tarasantoniuk.finance.core.counterparty.service.CounterpartyService;
import com.tarasantoniuk.finance.core.country.exception.CountryNotFoundException;
import com.tarasantoniuk.finance.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CounterpartyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CounterpartyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CounterpartyService counterpartyService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
        // Given - required field is null
        requestDto.setCode(null);

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
    void getAll_WithDefaultParameters_ShouldReturnPagedCounterparties() throws Exception {
        // Given
        CounterpartyResponseDto responseDto2 = new CounterpartyResponseDto();
        responseDto2.setId(2L);
        responseDto2.setCode("CP002");
        responseDto2.setName("Second Counterparty");

        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(1)
                .pageSize(50)
                .totalElements(2)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<CounterpartyResponseDto> pageResponse =
                PageResponse.<CounterpartyResponseDto>builder()
                        .content(List.of(responseDto, responseDto2))
                        .metadata(metadata)
                        .build();

        when(counterpartyService.getAll(0, 50)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/counterparties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].code").value("CP001"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].code").value("CP002"))
                .andExpect(jsonPath("$.metadata.currentPage").value(0))
                .andExpect(jsonPath("$.metadata.totalPages").value(1))
                .andExpect(jsonPath("$.metadata.pageSize").value(50))
                .andExpect(jsonPath("$.metadata.totalElements").value(2))
                .andExpect(jsonPath("$.metadata.hasNext").value(false))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(false));

        verify(counterpartyService, times(1)).getAll(0, 50);
    }

    @Test
    void getAll_WithCustomPageAndSize_ShouldReturnPagedCounterparties() throws Exception {
        // Given
        PageMetadata metadata = PageMetadata.builder()
                .currentPage(1)
                .totalPages(3)
                .pageSize(50)
                .totalElements(150)
                .hasNext(true)
                .hasPrevious(true)
                .build();

        PageResponse<CounterpartyResponseDto> pageResponse =
                PageResponse.<CounterpartyResponseDto>builder()
                        .content(List.of(responseDto))
                        .metadata(metadata)
                        .build();

        when(counterpartyService.getAll(1, 50)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/counterparties")
                        .param("page", "1")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.metadata.currentPage").value(1))
                .andExpect(jsonPath("$.metadata.totalPages").value(3))
                .andExpect(jsonPath("$.metadata.pageSize").value(50))
                .andExpect(jsonPath("$.metadata.totalElements").value(150))
                .andExpect(jsonPath("$.metadata.hasNext").value(true))
                .andExpect(jsonPath("$.metadata.hasPrevious").value(true));

        verify(counterpartyService, times(1)).getAll(1, 50);
    }

    @Test
    void getAll_WhenSizeExceedsMaxSize_ShouldLimitToMaxSize() throws Exception {
        // Given
        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(1)
                .pageSize(500)
                .totalElements(10)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<CounterpartyResponseDto> pageResponse =
                PageResponse.<CounterpartyResponseDto>builder()
                        .content(List.of(responseDto))
                        .metadata(metadata)
                        .build();

        // Requesting 1000 but should be limited to 500 (maxSize)
        when(counterpartyService.getAll(0, 500)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/counterparties")
                        .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.pageSize").value(500));

        verify(counterpartyService, times(1)).getAll(0, 500);
    }

    @Test
    void getAll_WhenEmpty_ShouldReturnEmptyPage() throws Exception {
        // Given
        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(0)
                .pageSize(50)
                .totalElements(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        PageResponse<CounterpartyResponseDto> pageResponse =
                PageResponse.<CounterpartyResponseDto>builder()
                        .content(List.of())
                        .metadata(metadata)
                        .build();

        when(counterpartyService.getAll(0, 50)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/counterparties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.metadata.totalElements").value(0));

        verify(counterpartyService, times(1)).getAll(0, 50);
    }

    @Test
    void getAll_WithOnlyPageParameter_ShouldUseDefaultSize() throws Exception {
        // Given
        PageMetadata metadata = PageMetadata.builder()
                .currentPage(2)
                .totalPages(5)
                .pageSize(50)
                .totalElements(250)
                .hasNext(true)
                .hasPrevious(true)
                .build();

        PageResponse<CounterpartyResponseDto> pageResponse =
                PageResponse.<CounterpartyResponseDto>builder()
                        .content(List.of(responseDto))
                        .metadata(metadata)
                        .build();

        when(counterpartyService.getAll(2, 50)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/counterparties")
                        .param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.pageSize").value(50));

        verify(counterpartyService, times(1)).getAll(2, 50);
    }

    @Test
    void getAll_WithOnlySizeParameter_ShouldUseDefaultPage() throws Exception {
        // Given
        PageMetadata metadata = PageMetadata.builder()
                .currentPage(0)
                .totalPages(10)
                .pageSize(20)
                .totalElements(200)
                .hasNext(true)
                .hasPrevious(false)
                .build();

        PageResponse<CounterpartyResponseDto> pageResponse =
                PageResponse.<CounterpartyResponseDto>builder()
                        .content(List.of(responseDto))
                        .metadata(metadata)
                        .build();

        when(counterpartyService.getAll(0, 20)).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/counterparties")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.currentPage").value(0));

        verify(counterpartyService, times(1)).getAll(0, 20);
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
    void activate_WhenExists_ShouldReturnOkWithBody() throws Exception {
        // Given
        CounterpartyResponseDto activatedResponse = new CounterpartyResponseDto();
        activatedResponse.setId(1L);
        activatedResponse.setCode("CP001");
        activatedResponse.setName("Test Counterparty");
        activatedResponse.setIsActive(true);

        when(counterpartyService.activate(1L)).thenReturn(activatedResponse);

        // When & Then
        mockMvc.perform(patch("/api/counterparties/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(counterpartyService, times(1)).activate(1L);
    }

    @Test
    void activate_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(counterpartyService.activate(1L)).thenThrow(CounterpartyNotFoundException.byId(1L));

        // When & Then
        mockMvc.perform(patch("/api/counterparties/1/activate"))
                .andExpect(status().isNotFound());

        verify(counterpartyService, times(1)).activate(1L);
    }

    // ========== DEACTIVATE TESTS ==========

    @Test
    void deactivate_WhenExists_ShouldReturnOkWithBody() throws Exception {
        // Given
        CounterpartyResponseDto deactivatedResponse = new CounterpartyResponseDto();
        deactivatedResponse.setId(1L);
        deactivatedResponse.setCode("CP001");
        deactivatedResponse.setName("Test Counterparty");
        deactivatedResponse.setIsActive(false);

        when(counterpartyService.deactivate(1L)).thenReturn(deactivatedResponse);

        // When & Then
        mockMvc.perform(patch("/api/counterparties/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(counterpartyService, times(1)).deactivate(1L);
    }

    @Test
    void deactivate_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(counterpartyService.deactivate(1L)).thenThrow(CounterpartyNotFoundException.byId(1L));

        // When & Then
        mockMvc.perform(patch("/api/counterparties/1/deactivate"))
                .andExpect(status().isNotFound());

        verify(counterpartyService, times(1)).deactivate(1L);
    }
}
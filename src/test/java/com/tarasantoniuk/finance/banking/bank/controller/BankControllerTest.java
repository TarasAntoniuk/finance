package com.tarasantoniuk.finance.banking.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarasantoniuk.finance.banking.bank.dto.BankRequestDto;
import com.tarasantoniuk.finance.banking.bank.dto.BankResponseDto;
import com.tarasantoniuk.finance.banking.bank.service.BankService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankController.class)
class BankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BankService bankService;

    @Test
    void getAllBanks_ShouldReturnListOfBanks() throws Exception {
        // Given
        BankResponseDto bank1 = new BankResponseDto();
        bank1.setId(1L);
        bank1.setName("PrivatBank");
        bank1.setSwiftCode("PBANUA2X");

        BankResponseDto bank2 = new BankResponseDto();
        bank2.setId(2L);
        bank2.setName("Monobank");
        bank2.setSwiftCode("MBNKUA2X");

        List<BankResponseDto> banks = Arrays.asList(bank1, bank2);
        when(bankService.getAllBanks()).thenReturn(banks);

        // When & Then
        mockMvc.perform(get("/api/banks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("PrivatBank"))
                .andExpect(jsonPath("$[1].name").value("Monobank"));
    }

    @Test
    void getBankById_WhenExists_ShouldReturnBank() throws Exception {
        // Given
        BankResponseDto bank = new BankResponseDto();
        bank.setId(1L);
        bank.setName("PrivatBank");
        bank.setSwiftCode("PBANUA2X");

        when(bankService.getBankById(1L)).thenReturn(bank);

        // When & Then
        mockMvc.perform(get("/api/banks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("PrivatBank"))
                .andExpect(jsonPath("$.swiftCode").value("PBANUA2X"));
    }

    @Test
    void getBanksByCountry_ShouldReturnFilteredList() throws Exception {
        // Given
        BankResponseDto bank = new BankResponseDto();
        bank.setId(1L);
        bank.setName("PrivatBank");

        when(bankService.getBanksByCountry(1L)).thenReturn(List.of(bank));

        // When & Then
        mockMvc.perform(get("/api/banks/country/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("PrivatBank"));
    }

    @Test
    void getActiveBanks_ShouldReturnActiveBanksOnly() throws Exception {
        // Given
        BankResponseDto bank = new BankResponseDto();
        bank.setId(1L);
        bank.setName("PrivatBank");
        bank.setIsActive(true);

        when(bankService.getActiveBanks()).thenReturn(List.of(bank));

        // When & Then
        mockMvc.perform(get("/api/banks/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void getBankBySwiftCode_WhenExists_ShouldReturnBank() throws Exception {
        // Given
        BankResponseDto bank = new BankResponseDto();
        bank.setId(1L);
        bank.setName("PrivatBank");
        bank.setSwiftCode("PBANUA2X");

        when(bankService.getBankBySwiftCode("PBANUA2X")).thenReturn(bank);

        // When & Then
        mockMvc.perform(get("/api/banks/swift/PBANUA2X"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("PBANUA2X"));
    }

    @Test
    void createBank_WhenValid_ShouldReturnCreated() throws Exception {
        // Given
        BankRequestDto requestDTO = new BankRequestDto();
        requestDTO.setName("PrivatBank");
        requestDTO.setSwiftCode("PBANUA2X");
        requestDTO.setCountryId(1L);
        requestDTO.setIsActive(true);

        BankResponseDto responseDTO = new BankResponseDto();
        responseDTO.setId(1L);
        responseDTO.setName("PrivatBank");
        responseDTO.setSwiftCode("PBANUA2X");

        when(bankService.createBank(any(BankRequestDto.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("PrivatBank"))
                .andExpect(jsonPath("$.swiftCode").value("PBANUA2X"));
    }

    @Test
    void createBank_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        // Given - missing required fields
        BankRequestDto requestDTO = new BankRequestDto();

        // When & Then
        mockMvc.perform(post("/api/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBank_WhenValid_ShouldReturnUpdated() throws Exception {
        // Given
        BankRequestDto requestDTO = new BankRequestDto();
        requestDTO.setName("PrivatBank Updated");
        requestDTO.setSwiftCode("PBANUA2X");
        requestDTO.setCountryId(1L);

        BankResponseDto responseDTO = new BankResponseDto();
        responseDTO.setId(1L);
        responseDTO.setName("PrivatBank Updated");
        responseDTO.setSwiftCode("PBANUA2X");

        when(bankService.updateBank(anyLong(), any(BankRequestDto.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/banks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("PrivatBank Updated"));
    }

    @Test
    void activateBank_WhenExists_ShouldReturnActivated() throws Exception {
        // Given
        BankResponseDto responseDTO = new BankResponseDto();
        responseDTO.setId(1L);
        responseDTO.setIsActive(true);

        when(bankService.activateBank(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/banks/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void deactivateBank_WhenExists_ShouldReturnDeactivated() throws Exception {
        // Given
        BankResponseDto responseDTO = new BankResponseDto();
        responseDTO.setId(1L);
        responseDTO.setIsActive(false);

        when(bankService.deactivateBank(1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/api/banks/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void deleteBank_WhenExists_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/banks/1"))
                .andExpect(status().isNoContent());
    }
}
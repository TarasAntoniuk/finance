package com.tarasantoniuk.finance.externalexchangerate.source.ecb;

import com.tarasantoniuk.finance.externalexchangerate.source.ecb.ECBClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ECBClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ECBClient client;

    private static final String SAMPLE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gesmes:Envelope xmlns:gesmes="http://www.gesmes.org/xml/2002-08-01" xmlns="http://www.ecb.int/vocabulary/2002-08-01/eurofxref">
                <Cube>
                    <Cube time='2024-10-20'>
                        <Cube currency='USD' rate='1.0850'/>
                        <Cube currency='GBP' rate='0.8345'/>
                    </Cube>
                </Cube>
            </gesmes:Envelope>
            """;

    @Test
    void fetchDaily_Success() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(SAMPLE_XML);

        Map<LocalDate, Map<String, BigDecimal>> result = client.fetchDaily();

        assertThat(result).hasSize(1);
        LocalDate date = LocalDate.of(2024, 10, 20);
        assertThat(result.get(date).get("USD")).isEqualByComparingTo("1.0850");
    }

    @Test
    void fetchDaily_NetworkError_ThrowsException() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThatThrownBy(() -> client.fetchDaily())
                .isInstanceOf(RuntimeException.class);
    }
}
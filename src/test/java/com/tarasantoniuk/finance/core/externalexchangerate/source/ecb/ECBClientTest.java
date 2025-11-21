package com.tarasantoniuk.finance.core.externalexchangerate.source.ecb;

import com.tarasantoniuk.finance.core.externalexchangerate.source.ecb.ECBClient;
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

    @Test
    void fetchHistory_Success() {
        // Given - XML with multiple dates (history format)
        String historyXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gesmes:Envelope xmlns:gesmes="http://www.gesmes.org/xml/2002-08-01" xmlns="http://www.ecb.int/vocabulary/2002-08-01/eurofxref">
                    <Cube>
                        <Cube time='2024-10-20'>
                            <Cube currency='USD' rate='1.0850'/>
                            <Cube currency='GBP' rate='0.8345'/>
                        </Cube>
                        <Cube time='2024-10-19'>
                            <Cube currency='USD' rate='1.0800'/>
                            <Cube currency='GBP' rate='0.8300'/>
                        </Cube>
                    </Cube>
                </gesmes:Envelope>
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(historyXml);

        // When
        Map<LocalDate, Map<String, BigDecimal>> result = client.fetchHistory();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(LocalDate.of(2024, 10, 20)).get("USD")).isEqualByComparingTo("1.0850");
        assertThat(result.get(LocalDate.of(2024, 10, 19)).get("USD")).isEqualByComparingTo("1.0800");
    }

    @Test
    void fetchHistory_NetworkError_ThrowsException() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then
        assertThatThrownBy(() -> client.fetchHistory())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ECB fetch failed");
    }

//    @Test
//    void parseXml_InvalidXml_ThrowsException() {
//        // Given - malformed XML
//        String invalidXml = "This is not valid XML at all";
//        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(invalidXml);
//
//        // When & Then
//        assertThatThrownBy(() -> client.fetchDaily())
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("ECB parse failed");
//    }

    @Test
    void parseXml_EmptyXml_ReturnsEmptyMap() {
        // Given - valid XML but no data
        String emptyXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gesmes:Envelope xmlns:gesmes="http://www.gesmes.org/xml/2002-08-01" xmlns="http://www.ecb.int/vocabulary/2002-08-01/eurofxref">
                    <Cube>
                    </Cube>
                </gesmes:Envelope>
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(emptyXml);

        // When
        Map<LocalDate, Map<String, BigDecimal>> result = client.fetchDaily();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void parseXml_CubesWithoutTimeAttribute_AreSkipped() {
        // Given - XML with cubes without time attribute
        String xmlWithoutTime = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gesmes:Envelope xmlns:gesmes="http://www.gesmes.org/xml/2002-08-01" xmlns="http://www.ecb.int/vocabulary/2002-08-01/eurofxref">
                    <Cube>
                        <Cube>
                            <Cube currency='USD' rate='1.0850'/>
                        </Cube>
                        <Cube time='2024-10-20'>
                            <Cube currency='GBP' rate='0.8345'/>
                        </Cube>
                    </Cube>
                </gesmes:Envelope>
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(xmlWithoutTime);

        // When
        Map<LocalDate, Map<String, BigDecimal>> result = client.fetchDaily();

        // Then - only the cube with time attribute should be included
        assertThat(result).hasSize(1);
        assertThat(result.get(LocalDate.of(2024, 10, 20)).get("GBP")).isEqualByComparingTo("0.8345");
    }

    @Test
    void parseXml_DateWithNoRates_IsNotIncluded() {
        // Given - date cube without any currency rates
        String xmlWithEmptyDate = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gesmes:Envelope xmlns:gesmes="http://www.gesmes.org/xml/2002-08-01" xmlns="http://www.ecb.int/vocabulary/2002-08-01/eurofxref">
                    <Cube>
                        <Cube time='2024-10-20'>
                        </Cube>
                        <Cube time='2024-10-19'>
                            <Cube currency='USD' rate='1.0800'/>
                        </Cube>
                    </Cube>
                </gesmes:Envelope>
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(xmlWithEmptyDate);

        // When
        Map<LocalDate, Map<String, BigDecimal>> result = client.fetchDaily();

        // Then - only date with rates should be included
        assertThat(result).hasSize(1);
        assertThat(result).containsKey(LocalDate.of(2024, 10, 19));
        assertThat(result).doesNotContainKey(LocalDate.of(2024, 10, 20));
    }

    @Test
    void parseXml_MultipleCurrencies_AllParsedCorrectly() {
        // Given - XML with many currencies
        String xmlMultipleCurrencies = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gesmes:Envelope xmlns:gesmes="http://www.gesmes.org/xml/2002-08-01" xmlns="http://www.ecb.int/vocabulary/2002-08-01/eurofxref">
                    <Cube>
                        <Cube time='2024-10-20'>
                            <Cube currency='USD' rate='1.0850'/>
                            <Cube currency='GBP' rate='0.8345'/>
                            <Cube currency='JPY' rate='162.50'/>
                            <Cube currency='CHF' rate='0.9420'/>
                        </Cube>
                    </Cube>
                </gesmes:Envelope>
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(xmlMultipleCurrencies);

        // When
        Map<LocalDate, Map<String, BigDecimal>> result = client.fetchDaily();

        // Then
        assertThat(result).hasSize(1);
        Map<String, BigDecimal> rates = result.get(LocalDate.of(2024, 10, 20));
        assertThat(rates).hasSize(4);
        assertThat(rates.get("USD")).isEqualByComparingTo("1.0850");
        assertThat(rates.get("GBP")).isEqualByComparingTo("0.8345");
        assertThat(rates.get("JPY")).isEqualByComparingTo("162.50");
        assertThat(rates.get("CHF")).isEqualByComparingTo("0.9420");
    }

    @Test
    void parseXml_LargeHistoryDataset_ParsesSuccessfully() {
        // Given - simulating large history with 3 dates
        String largeHistoryXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <gesmes:Envelope xmlns:gesmes="http://www.gesmes.org/xml/2002-08-01" xmlns="http://www.ecb.int/vocabulary/2002-08-01/eurofxref">
                    <Cube>
                        <Cube time='2024-10-20'>
                            <Cube currency='USD' rate='1.0850'/>
                            <Cube currency='GBP' rate='0.8345'/>
                            <Cube currency='JPY' rate='162.50'/>
                        </Cube>
                        <Cube time='2024-10-19'>
                            <Cube currency='USD' rate='1.0800'/>
                            <Cube currency='GBP' rate='0.8300'/>
                            <Cube currency='JPY' rate='161.80'/>
                        </Cube>
                        <Cube time='2024-10-18'>
                            <Cube currency='USD' rate='1.0780'/>
                            <Cube currency='GBP' rate='0.8280'/>
                            <Cube currency='JPY' rate='161.20'/>
                        </Cube>
                    </Cube>
                </gesmes:Envelope>
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(largeHistoryXml);

        // When
        Map<LocalDate, Map<String, BigDecimal>> result = client.fetchHistory();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(LocalDate.of(2024, 10, 20))).hasSize(3);
        assertThat(result.get(LocalDate.of(2024, 10, 19))).hasSize(3);
        assertThat(result.get(LocalDate.of(2024, 10, 18))).hasSize(3);
    }
}
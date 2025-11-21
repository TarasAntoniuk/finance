package com.tarasantoniuk.finance.core.externalexchangerate.source.ecb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class ECBClient {

    private static final Logger log = LoggerFactory.getLogger(ECBClient.class);
    private static final String DAILY_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private static final String HISTORY_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist.xml";

    private final RestTemplate restTemplate;

    public ECBClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<LocalDate, Map<String, BigDecimal>> fetchDaily() {
        return fetch(DAILY_URL);
    }

    public Map<LocalDate, Map<String, BigDecimal>> fetchHistory() {
        return fetch(HISTORY_URL);
    }

    private Map<LocalDate, Map<String, BigDecimal>> fetch(String url) {
        try {
            String xml = restTemplate.getForObject(url, String.class);
            return parseXml(xml);
        } catch (Exception e) {
            log.error("Failed to fetch from ECB: {}", url, e);
            throw new RuntimeException("ECB fetch failed", e);
        }
    }

    private Map<LocalDate, Map<String, BigDecimal>> parseXml(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes()));

            Map<LocalDate, Map<String, BigDecimal>> result = new HashMap<>();
            NodeList cubes = doc.getElementsByTagName("Cube");

            for (int i = 0; i < cubes.getLength(); i++) {
                Element cube = (Element) cubes.item(i);
                if (!cube.hasAttribute("time")) continue;

                LocalDate date = LocalDate.parse(cube.getAttribute("time"));
                Map<String, BigDecimal> rates = getStringBigDecimalMap(cube);

                if (!rates.isEmpty()) {
                    result.put(date, rates);
                }
            }

            log.info("Parsed {} dates with {} total rates from ECB",
                    result.size(), result.values().stream().mapToInt(Map::size).sum());
            return result;

        } catch (Exception e) {
            log.error("Failed to parse ECB XML", e);
            throw new RuntimeException("ECB parse failed", e);
        }
    }

    private static Map<String, BigDecimal> getStringBigDecimalMap(Element cube) {
        Map<String, BigDecimal> rates = new HashMap<>();

        NodeList children = cube.getElementsByTagName("Cube");
        for (int j = 0; j < children.getLength(); j++) {
            Element child = (Element) children.item(j);
            if (child.hasAttribute("currency")) {
                rates.put(
                        child.getAttribute("currency"),
                        new BigDecimal(child.getAttribute("rate"))
                );
            }
        }
        return rates;
    }
}
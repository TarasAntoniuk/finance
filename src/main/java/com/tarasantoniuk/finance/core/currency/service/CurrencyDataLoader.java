package com.tarasantoniuk.finance.core.currency.service;

import com.tarasantoniuk.finance.core.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.core.currency.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads initial currency list on application startup.
 * Executes only if database is empty.
 */
@Component
@Order(1)
public class CurrencyDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CurrencyDataLoader.class);

    private final CurrencyRepository currencyRepository;
    private final CurrencyService currencyService;

    public CurrencyDataLoader(CurrencyRepository currencyRepository,
                              CurrencyService currencyService) {
        this.currencyRepository = currencyRepository;
        this.currencyService = currencyService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (currencyRepository.count() > 0) {
            log.info("Currencies already loaded. Skipping initialization.");
            return;
        }

        log.info("Loading initial currency data...");

        List<CurrencyRequestDTO> currencies = getInitialCurrencies();
        int loaded = 0;
        int failed = 0;

        for (CurrencyRequestDTO dto : currencies) {
            try {
                currencyService.createCurrency(dto);
                loaded++;
            } catch (Exception e) {
                log.error("Failed to load currency {}: {}", dto.getCode(), e.getMessage());
                failed++;
            }
        }

        log.info("Currency loading completed. Loaded: {}, Failed: {}", loaded, failed);
    }

    private CurrencyRequestDTO createCurrency(String code, String numericCode, String name,
                                              String symbol, Integer minorUnit, Boolean isActive) {
        CurrencyRequestDTO dto = new CurrencyRequestDTO();
        dto.setCode(code);
        dto.setNumericCode(numericCode);
        dto.setName(name);
        dto.setSymbol(symbol);
        dto.setMinorUnit(minorUnit);
        dto.setIsActive(isActive);
        return dto;
    }

    private List<CurrencyRequestDTO> getInitialCurrencies() {
        List<CurrencyRequestDTO> currencies = new ArrayList<>();

        // Major world currencies
        currencies.add(createCurrency("USD", "840", "US Dollar", "$", 2, true));
        currencies.add(createCurrency("EUR", "978", "Euro", "€", 2, true));
        currencies.add(createCurrency("GBP", "826", "Pound Sterling", "£", 2, true));
        currencies.add(createCurrency("JPY", "392", "Yen", "¥", 0, true));
        currencies.add(createCurrency("CHF", "756", "Swiss Franc", "CHF", 2, true));
        currencies.add(createCurrency("CAD", "124", "Canadian Dollar", "C$", 2, true));
        currencies.add(createCurrency("AUD", "036", "Australian Dollar", "A$", 2, true));
        currencies.add(createCurrency("NZD", "554", "New Zealand Dollar", "NZ$", 2, true));

        // European currencies
        currencies.add(createCurrency("UAH", "980", "Hryvnia", "₴", 2, true));
        currencies.add(createCurrency("PLN", "985", "Zloty", "zł", 2, true));
        currencies.add(createCurrency("CZK", "203", "Czech Koruna", "Kč", 2, true));
        currencies.add(createCurrency("HUF", "348", "Forint", "Ft", 2, true));
        currencies.add(createCurrency("RON", "946", "Romanian Leu", "lei", 2, true));
        currencies.add(createCurrency("BGN", "975", "Bulgarian Lev", "лв", 2, true));
        currencies.add(createCurrency("HRK", "191", "Kuna", "kn", 2, true));
        currencies.add(createCurrency("DKK", "208", "Danish Krone", "kr", 2, true));
        currencies.add(createCurrency("SEK", "752", "Swedish Krona", "kr", 2, true));
        currencies.add(createCurrency("NOK", "578", "Norwegian Krone", "kr", 2, true));
        currencies.add(createCurrency("ISK", "352", "Iceland Krona", "kr", 0, true));
        currencies.add(createCurrency("TRY", "949", "Turkish Lira", "₺", 2, true));
        currencies.add(createCurrency("RUB", "643", "Russian Ruble", "₽", 2, true));
        currencies.add(createCurrency("BYN", "933", "Belarusian Ruble", "Br", 2, true));
        currencies.add(createCurrency("MDL", "498", "Moldovan Leu", "L", 2, true));
        currencies.add(createCurrency("GEL", "981", "Lari", "₾", 2, true));
        currencies.add(createCurrency("AMD", "051", "Armenian Dram", "֏", 2, true));
        currencies.add(createCurrency("AZN", "944", "Azerbaijan Manat", "₼", 2, true));
        currencies.add(createCurrency("RSD", "941", "Serbian Dinar", "дин", 2, true));
        currencies.add(createCurrency("MKD", "807", "Denar", "ден", 2, true));
        currencies.add(createCurrency("ALL", "008", "Lek", "L", 2, true));
        currencies.add(createCurrency("BAM", "977", "Convertible Mark", "KM", 2, true));

        // Asian currencies
        currencies.add(createCurrency("CNY", "156", "Yuan Renminbi", "¥", 2, true));
        currencies.add(createCurrency("HKD", "344", "Hong Kong Dollar", "HK$", 2, true));
        currencies.add(createCurrency("SGD", "702", "Singapore Dollar", "S$", 2, true));
        currencies.add(createCurrency("KRW", "410", "Won", "₩", 0, true));
        currencies.add(createCurrency("INR", "356", "Indian Rupee", "₹", 2, true));
        currencies.add(createCurrency("IDR", "360", "Rupiah", "Rp", 2, true));
        currencies.add(createCurrency("MYR", "458", "Malaysian Ringgit", "RM", 2, true));
        currencies.add(createCurrency("PHP", "608", "Philippine Peso", "₱", 2, true));
        currencies.add(createCurrency("THB", "764", "Baht", "฿", 2, true));
        currencies.add(createCurrency("VND", "704", "Dong", "₫", 0, true));
        currencies.add(createCurrency("PKR", "586", "Pakistan Rupee", "₨", 2, true));
        currencies.add(createCurrency("BDT", "050", "Taka", "৳", 2, true));
        currencies.add(createCurrency("LKR", "144", "Sri Lanka Rupee", "Rs", 2, true));
        currencies.add(createCurrency("NPR", "524", "Nepalese Rupee", "Rs", 2, true));
        currencies.add(createCurrency("MMK", "104", "Kyat", "K", 2, true));
        currencies.add(createCurrency("KHR", "116", "Riel", "៛", 2, true));
        currencies.add(createCurrency("LAK", "418", "Lao Kip", "₭", 2, true));
        currencies.add(createCurrency("BND", "096", "Brunei Dollar", "B$", 2, true));
        currencies.add(createCurrency("TWD", "901", "New Taiwan Dollar", "NT$", 2, true));
        currencies.add(createCurrency("MNT", "496", "Tugrik", "₮", 2, true));
        currencies.add(createCurrency("KZT", "398", "Tenge", "₸", 2, true));
        currencies.add(createCurrency("UZS", "860", "Uzbekistan Sum", "soʻm", 2, true));
        currencies.add(createCurrency("KGS", "417", "Som", "с", 2, true));
        currencies.add(createCurrency("TJS", "972", "Somoni", "ЅМ", 2, true));
        currencies.add(createCurrency("TMT", "934", "Turkmenistan Manat", "m", 2, true));
        currencies.add(createCurrency("AFN", "971", "Afghani", "؋", 2, true));

        // Middle Eastern currencies
        currencies.add(createCurrency("ILS", "376", "New Israeli Sheqel", "₪", 2, true));
        currencies.add(createCurrency("SAR", "682", "Saudi Riyal", "ر.س", 2, true));
        currencies.add(createCurrency("AED", "784", "UAE Dirham", "د.إ", 2, true));
        currencies.add(createCurrency("QAR", "634", "Qatari Rial", "ر.ق", 2, true));
        currencies.add(createCurrency("KWD", "414", "Kuwaiti Dinar", "د.ك", 3, true));
        currencies.add(createCurrency("BHD", "048", "Bahraini Dinar", "د.ب", 3, true));
        currencies.add(createCurrency("OMR", "512", "Rial Omani", "ر.ع.", 3, true));
        currencies.add(createCurrency("JOD", "400", "Jordanian Dinar", "د.ا", 3, true));
        currencies.add(createCurrency("LBP", "422", "Lebanese Pound", "ل.ل", 2, true));
        currencies.add(createCurrency("SYP", "760", "Syrian Pound", "ل.س", 2, true));
        currencies.add(createCurrency("IQD", "368", "Iraqi Dinar", "ع.د", 3, true));
        currencies.add(createCurrency("IRR", "364", "Iranian Rial", "ریال", 2, true));
        currencies.add(createCurrency("YER", "886", "Yemeni Rial", "ر.ي", 2, true));

        // African currencies
        currencies.add(createCurrency("ZAR", "710", "Rand", "R", 2, true));
        currencies.add(createCurrency("EGP", "818", "Egyptian Pound", "£", 2, true));
        currencies.add(createCurrency("NGN", "566", "Naira", "₦", 2, true));
        currencies.add(createCurrency("KES", "404", "Kenyan Shilling", "Sh", 2, true));
        currencies.add(createCurrency("GHS", "936", "Ghana Cedi", "₵", 2, true));
        currencies.add(createCurrency("TZS", "834", "Tanzanian Shilling", "Sh", 2, true));
        currencies.add(createCurrency("UGX", "800", "Uganda Shilling", "Sh", 0, true));
        currencies.add(createCurrency("ETB", "230", "Ethiopian Birr", "Br", 2, true));
        currencies.add(createCurrency("MAD", "504", "Moroccan Dirham", "د.م.", 2, true));
        currencies.add(createCurrency("TND", "788", "Tunisian Dinar", "د.ت", 3, true));
        currencies.add(createCurrency("DZD", "012", "Algerian Dinar", "د.ج", 2, true));
        currencies.add(createCurrency("LYD", "434", "Libyan Dinar", "ل.د", 3, true));
        currencies.add(createCurrency("XOF", "952", "CFA Franc BCEAO", "Fr", 0, true));
        currencies.add(createCurrency("XAF", "950", "CFA Franc BEAC", "Fr", 0, true));
        currencies.add(createCurrency("MUR", "480", "Mauritius Rupee", "₨", 2, true));
        currencies.add(createCurrency("SCR", "690", "Seychelles Rupee", "₨", 2, true));
        currencies.add(createCurrency("BWP", "072", "Pula", "P", 2, true));
        currencies.add(createCurrency("NAD", "516", "Namibia Dollar", "N$", 2, true));
        currencies.add(createCurrency("MZN", "943", "Mozambique Metical", "MT", 2, true));
        currencies.add(createCurrency("ZMW", "967", "Zambian Kwacha", "ZK", 2, true));
        currencies.add(createCurrency("AOA", "973", "Kwanza", "Kz", 2, true));

        // American currencies
        currencies.add(createCurrency("MXN", "484", "Mexican Peso", "$", 2, true));
        currencies.add(createCurrency("BRL", "986", "Brazilian Real", "R$", 2, true));
        currencies.add(createCurrency("ARS", "032", "Argentine Peso", "$", 2, true));
        currencies.add(createCurrency("CLP", "152", "Chilean Peso", "$", 0, true));
        currencies.add(createCurrency("COP", "170", "Colombian Peso", "$", 2, true));
        currencies.add(createCurrency("PEN", "604", "Sol", "S/", 2, true));
        currencies.add(createCurrency("VES", "928", "Bolívar Soberano", "Bs.", 2, true));
        currencies.add(createCurrency("UYU", "858", "Peso Uruguayo", "$", 2, true));
        currencies.add(createCurrency("PYG", "600", "Guarani", "₲", 0, true));
        currencies.add(createCurrency("BOB", "068", "Boliviano", "Bs.", 2, true));
        currencies.add(createCurrency("CRC", "188", "Costa Rican Colon", "₡", 2, true));
        currencies.add(createCurrency("GTQ", "320", "Quetzal", "Q", 2, true));
        currencies.add(createCurrency("HNL", "340", "Lempira", "L", 2, true));
        currencies.add(createCurrency("NIO", "558", "Cordoba Oro", "C$", 2, true));
        currencies.add(createCurrency("PAB", "590", "Balboa", "B/.", 2, true));
        currencies.add(createCurrency("DOP", "214", "Dominican Peso", "RD$", 2, true));
        currencies.add(createCurrency("JMD", "388", "Jamaican Dollar", "J$", 2, true));
        currencies.add(createCurrency("TTD", "780", "Trinidad and Tobago Dollar", "TT$", 2, true));
        currencies.add(createCurrency("BBD", "052", "Barbados Dollar", "Bds$", 2, true));
        currencies.add(createCurrency("BSD", "044", "Bahamian Dollar", "B$", 2, true));
        currencies.add(createCurrency("BZD", "084", "Belize Dollar", "BZ$", 2, true));
        currencies.add(createCurrency("HTG", "332", "Gourde", "G", 2, true));
        currencies.add(createCurrency("AWG", "533", "Aruban Florin", "ƒ", 2, true));
        currencies.add(createCurrency("ANG", "532", "Netherlands Antillean Guilder", "ƒ", 2, true));
        currencies.add(createCurrency("XCD", "951", "East Caribbean Dollar", "$", 2, true));
        currencies.add(createCurrency("SRD", "968", "Surinam Dollar", "$", 2, true));
        currencies.add(createCurrency("GYD", "328", "Guyana Dollar", "G$", 2, true));

        // Oceania
        currencies.add(createCurrency("FJD", "242", "Fiji Dollar", "FJ$", 2, true));
        currencies.add(createCurrency("PGK", "598", "Kina", "K", 2, true));
        currencies.add(createCurrency("SBD", "090", "Solomon Islands Dollar", "SI$", 2, true));
        currencies.add(createCurrency("VUV", "548", "Vatu", "Vt", 0, true));
        currencies.add(createCurrency("TOP", "776", "Pa'anga", "T$", 2, true));
        currencies.add(createCurrency("WST", "882", "Tala", "WS$", 2, true));
        currencies.add(createCurrency("XPF", "953", "CFP Franc", "Fr", 0, true));

        // Cryptocurrencies (optional)
        currencies.add(createCurrency("BTC", "000", "Bitcoin", "₿", 8, false));
        currencies.add(createCurrency("ETH", "001", "Ethereum", "Ξ", 18, false));

        // Special codes
        currencies.add(createCurrency("XXX", "999", "No currency", "", 0, false));
        currencies.add(createCurrency("XAU", "959", "Gold", "", 0, true));
        currencies.add(createCurrency("XAG", "961", "Silver", "", 0, true));
        currencies.add(createCurrency("XPT", "962", "Platinum", "", 0, true));
        currencies.add(createCurrency("XPD", "964", "Palladium", "", 0, true));

        return currencies;
    }

}
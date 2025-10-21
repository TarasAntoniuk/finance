package com.tarasantoniuk.finance.currency.service;

import com.tarasantoniuk.finance.currency.dto.CurrencyRequestDTO;
import com.tarasantoniuk.finance.currency.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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

    private List<CurrencyRequestDTO> getInitialCurrencies() {
        return Arrays.asList(
                // Major world currencies
                new CurrencyRequestDTO("USD", "840", "US Dollar", "$", 2, true),
                new CurrencyRequestDTO("EUR", "978", "Euro", "€", 2, true),
                new CurrencyRequestDTO("GBP", "826", "Pound Sterling", "£", 2, true),
                new CurrencyRequestDTO("JPY", "392", "Yen", "¥", 0, true),
                new CurrencyRequestDTO("CHF", "756", "Swiss Franc", "CHF", 2, true),
                new CurrencyRequestDTO("CAD", "124", "Canadian Dollar", "C$", 2, true),
                new CurrencyRequestDTO("AUD", "036", "Australian Dollar", "A$", 2, true),
                new CurrencyRequestDTO("NZD", "554", "New Zealand Dollar", "NZ$", 2, true),

                // European currencies
                new CurrencyRequestDTO("UAH", "980", "Hryvnia", "₴", 2, true),
                new CurrencyRequestDTO("PLN", "985", "Zloty", "zł", 2, true),
                new CurrencyRequestDTO("CZK", "203", "Czech Koruna", "Kč", 2, true),
                new CurrencyRequestDTO("HUF", "348", "Forint", "Ft", 2, true),
                new CurrencyRequestDTO("RON", "946", "Romanian Leu", "lei", 2, true),
                new CurrencyRequestDTO("BGN", "975", "Bulgarian Lev", "лв", 2, true),
                new CurrencyRequestDTO("HRK", "191", "Kuna", "kn", 2, true),
                new CurrencyRequestDTO("DKK", "208", "Danish Krone", "kr", 2, true),
                new CurrencyRequestDTO("SEK", "752", "Swedish Krona", "kr", 2, true),
                new CurrencyRequestDTO("NOK", "578", "Norwegian Krone", "kr", 2, true),
                new CurrencyRequestDTO("ISK", "352", "Iceland Krona", "kr", 0, true),
                new CurrencyRequestDTO("TRY", "949", "Turkish Lira", "₺", 2, true),
                new CurrencyRequestDTO("RUB", "643", "Russian Ruble", "₽", 2, true),
                new CurrencyRequestDTO("BYN", "933", "Belarusian Ruble", "Br", 2, true),
                new CurrencyRequestDTO("MDL", "498", "Moldovan Leu", "L", 2, true),
                new CurrencyRequestDTO("GEL", "981", "Lari", "₾", 2, true),
                new CurrencyRequestDTO("AMD", "051", "Armenian Dram", "֏", 2, true),
                new CurrencyRequestDTO("AZN", "944", "Azerbaijan Manat", "₼", 2, true),
                new CurrencyRequestDTO("RSD", "941", "Serbian Dinar", "дин", 2, true),
                new CurrencyRequestDTO("MKD", "807", "Denar", "ден", 2, true),
                new CurrencyRequestDTO("ALL", "008", "Lek", "L", 2, true),
                new CurrencyRequestDTO("BAM", "977", "Convertible Mark", "KM", 2, true),

                // Asian currencies
                new CurrencyRequestDTO("CNY", "156", "Yuan Renminbi", "¥", 2, true),
                new CurrencyRequestDTO("HKD", "344", "Hong Kong Dollar", "HK$", 2, true),
                new CurrencyRequestDTO("SGD", "702", "Singapore Dollar", "S$", 2, true),
                new CurrencyRequestDTO("KRW", "410", "Won", "₩", 0, true),
                new CurrencyRequestDTO("INR", "356", "Indian Rupee", "₹", 2, true),
                new CurrencyRequestDTO("IDR", "360", "Rupiah", "Rp", 2, true),
                new CurrencyRequestDTO("MYR", "458", "Malaysian Ringgit", "RM", 2, true),
                new CurrencyRequestDTO("PHP", "608", "Philippine Peso", "₱", 2, true),
                new CurrencyRequestDTO("THB", "764", "Baht", "฿", 2, true),
                new CurrencyRequestDTO("VND", "704", "Dong", "₫", 0, true),
                new CurrencyRequestDTO("PKR", "586", "Pakistan Rupee", "₨", 2, true),
                new CurrencyRequestDTO("BDT", "050", "Taka", "৳", 2, true),
                new CurrencyRequestDTO("LKR", "144", "Sri Lanka Rupee", "Rs", 2, true),
                new CurrencyRequestDTO("NPR", "524", "Nepalese Rupee", "Rs", 2, true),
                new CurrencyRequestDTO("MMK", "104", "Kyat", "K", 2, true),
                new CurrencyRequestDTO("KHR", "116", "Riel", "៛", 2, true),
                new CurrencyRequestDTO("LAK", "418", "Lao Kip", "₭", 2, true),
                new CurrencyRequestDTO("BND", "096", "Brunei Dollar", "B$", 2, true),
                new CurrencyRequestDTO("TWD", "901", "New Taiwan Dollar", "NT$", 2, true),
                new CurrencyRequestDTO("MNT", "496", "Tugrik", "₮", 2, true),
                new CurrencyRequestDTO("KZT", "398", "Tenge", "₸", 2, true),
                new CurrencyRequestDTO("UZS", "860", "Uzbekistan Sum", "soʻm", 2, true),
                new CurrencyRequestDTO("KGS", "417", "Som", "с", 2, true),
                new CurrencyRequestDTO("TJS", "972", "Somoni", "ЅМ", 2, true),
                new CurrencyRequestDTO("TMT", "934", "Turkmenistan Manat", "m", 2, true),
                new CurrencyRequestDTO("AFN", "971", "Afghani", "؋", 2, true),

                // Middle Eastern currencies
                new CurrencyRequestDTO("ILS", "376", "New Israeli Sheqel", "₪", 2, true),
                new CurrencyRequestDTO("SAR", "682", "Saudi Riyal", "ر.س", 2, true),
                new CurrencyRequestDTO("AED", "784", "UAE Dirham", "د.إ", 2, true),
                new CurrencyRequestDTO("QAR", "634", "Qatari Rial", "ر.ق", 2, true),
                new CurrencyRequestDTO("KWD", "414", "Kuwaiti Dinar", "د.ك", 3, true),
                new CurrencyRequestDTO("BHD", "048", "Bahraini Dinar", "د.ب", 3, true),
                new CurrencyRequestDTO("OMR", "512", "Rial Omani", "ر.ع.", 3, true),
                new CurrencyRequestDTO("JOD", "400", "Jordanian Dinar", "د.ا", 3, true),
                new CurrencyRequestDTO("LBP", "422", "Lebanese Pound", "ل.ل", 2, true),
                new CurrencyRequestDTO("SYP", "760", "Syrian Pound", "ل.س", 2, true),
                new CurrencyRequestDTO("IQD", "368", "Iraqi Dinar", "ع.د", 3, true),
                new CurrencyRequestDTO("IRR", "364", "Iranian Rial", "ریال", 2, true),
                new CurrencyRequestDTO("YER", "886", "Yemeni Rial", "ر.ي", 2, true),

                // African currencies
                new CurrencyRequestDTO("ZAR", "710", "Rand", "R", 2, true),
                new CurrencyRequestDTO("EGP", "818", "Egyptian Pound", "£", 2, true),
                new CurrencyRequestDTO("NGN", "566", "Naira", "₦", 2, true),
                new CurrencyRequestDTO("KES", "404", "Kenyan Shilling", "Sh", 2, true),
                new CurrencyRequestDTO("GHS", "936", "Ghana Cedi", "₵", 2, true),
                new CurrencyRequestDTO("TZS", "834", "Tanzanian Shilling", "Sh", 2, true),
                new CurrencyRequestDTO("UGX", "800", "Uganda Shilling", "Sh", 0, true),
                new CurrencyRequestDTO("ETB", "230", "Ethiopian Birr", "Br", 2, true),
                new CurrencyRequestDTO("MAD", "504", "Moroccan Dirham", "د.م.", 2, true),
                new CurrencyRequestDTO("TND", "788", "Tunisian Dinar", "د.ت", 3, true),
                new CurrencyRequestDTO("DZD", "012", "Algerian Dinar", "د.ج", 2, true),
                new CurrencyRequestDTO("LYD", "434", "Libyan Dinar", "ل.د", 3, true),
                new CurrencyRequestDTO("XOF", "952", "CFA Franc BCEAO", "Fr", 0, true),
                new CurrencyRequestDTO("XAF", "950", "CFA Franc BEAC", "Fr", 0, true),
                new CurrencyRequestDTO("MUR", "480", "Mauritius Rupee", "₨", 2, true),
                new CurrencyRequestDTO("SCR", "690", "Seychelles Rupee", "₨", 2, true),
                new CurrencyRequestDTO("BWP", "072", "Pula", "P", 2, true),
                new CurrencyRequestDTO("NAD", "516", "Namibia Dollar", "N$", 2, true),
                new CurrencyRequestDTO("MZN", "943", "Mozambique Metical", "MT", 2, true),
                new CurrencyRequestDTO("ZMW", "967", "Zambian Kwacha", "ZK", 2, true),
                new CurrencyRequestDTO("AOA", "973", "Kwanza", "Kz", 2, true),

                // American currencies
                new CurrencyRequestDTO("MXN", "484", "Mexican Peso", "$", 2, true),
                new CurrencyRequestDTO("BRL", "986", "Brazilian Real", "R$", 2, true),
                new CurrencyRequestDTO("ARS", "032", "Argentine Peso", "$", 2, true),
                new CurrencyRequestDTO("CLP", "152", "Chilean Peso", "$", 0, true),
                new CurrencyRequestDTO("COP", "170", "Colombian Peso", "$", 2, true),
                new CurrencyRequestDTO("PEN", "604", "Sol", "S/", 2, true),
                new CurrencyRequestDTO("VES", "928", "Bolívar Soberano", "Bs.", 2, true),
                new CurrencyRequestDTO("UYU", "858", "Peso Uruguayo", "$", 2, true),
                new CurrencyRequestDTO("PYG", "600", "Guarani", "₲", 0, true),
                new CurrencyRequestDTO("BOB", "068", "Boliviano", "Bs.", 2, true),
                new CurrencyRequestDTO("CRC", "188", "Costa Rican Colon", "₡", 2, true),
                new CurrencyRequestDTO("GTQ", "320", "Quetzal", "Q", 2, true),
                new CurrencyRequestDTO("HNL", "340", "Lempira", "L", 2, true),
                new CurrencyRequestDTO("NIO", "558", "Cordoba Oro", "C$", 2, true),
                new CurrencyRequestDTO("PAB", "590", "Balboa", "B/.", 2, true),
                new CurrencyRequestDTO("DOP", "214", "Dominican Peso", "RD$", 2, true),
                new CurrencyRequestDTO("JMD", "388", "Jamaican Dollar", "J$", 2, true),
                new CurrencyRequestDTO("TTD", "780", "Trinidad and Tobago Dollar", "TT$", 2, true),
                new CurrencyRequestDTO("BBD", "052", "Barbados Dollar", "Bds$", 2, true),
                new CurrencyRequestDTO("BSD", "044", "Bahamian Dollar", "B$", 2, true),
                new CurrencyRequestDTO("BZD", "084", "Belize Dollar", "BZ$", 2, true),
                new CurrencyRequestDTO("HTG", "332", "Gourde", "G", 2, true),
                new CurrencyRequestDTO("AWG", "533", "Aruban Florin", "ƒ", 2, true),
                new CurrencyRequestDTO("ANG", "532", "Netherlands Antillean Guilder", "ƒ", 2, true),
                new CurrencyRequestDTO("XCD", "951", "East Caribbean Dollar", "$", 2, true),
                new CurrencyRequestDTO("SRD", "968", "Surinam Dollar", "$", 2, true),
                new CurrencyRequestDTO("GYD", "328", "Guyana Dollar", "G$", 2, true),

                // Oceania
                new CurrencyRequestDTO("FJD", "242", "Fiji Dollar", "FJ$", 2, true),
                new CurrencyRequestDTO("PGK", "598", "Kina", "K", 2, true),
                new CurrencyRequestDTO("SBD", "090", "Solomon Islands Dollar", "SI$", 2, true),
                new CurrencyRequestDTO("VUV", "548", "Vatu", "Vt", 0, true),
                new CurrencyRequestDTO("TOP", "776", "Pa'anga", "T$", 2, true),
                new CurrencyRequestDTO("WST", "882", "Tala", "WS$", 2, true),
                new CurrencyRequestDTO("XPF", "953", "CFP Franc", "Fr", 0, true),

                // Cryptocurrencies (optional)
                new CurrencyRequestDTO("BTC", "000", "Bitcoin", "₿", 8, false),
                new CurrencyRequestDTO("ETH", "001", "Ethereum", "Ξ", 18, false),

                // Special codes
                new CurrencyRequestDTO("XXX", "999", "No currency", "", 0, false),
                new CurrencyRequestDTO("XAU", "959", "Gold", "", 0, true),
                new CurrencyRequestDTO("XAG", "961", "Silver", "", 0, true),
                new CurrencyRequestDTO("XPT", "962", "Platinum", "", 0, true),
                new CurrencyRequestDTO("XPD", "964", "Palladium", "", 0, true)
        );
    }
}
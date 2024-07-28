// src/test/java/com/scribe/currency/CurrencyServiceIntegrationTest.java
package com.scribe.currency;

import com.scribe.currency.dto.CurrencyDto;
import com.scribe.currency.dto.CurrencyExchangeDto;
import com.scribe.currency.dto.RateDto;
import com.scribe.currency.service.CurrencyService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CurrencyServiceIntegrationTest {
    public static final String TEST_KEY = "test";
    public static final String BASE_URL = "http://localhost:%d/api/%s";
    public static final String CURRENCIES_URL = "currencies";
    public static final String EXCHANGE_URL = "currencies/exchange/USD";
    public static final String EUR_CODE = "EUR";
    public static final String EUR_NAME = "Euro";
    public static final String USD_CODE = "USD";
    public static final String USD_NAME = "United States Dollar";
    @Autowired
    private CurrencyService currencyService;
    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;
    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    @Container
    public static WireMockContainer wiremockServer = new WireMockContainer(
            DockerImageName.parse("wiremock/wiremock:latest").asCompatibleSubstituteFor("wiremock/wiremock"))
            .withExposedPorts(8080)
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forHttp("/__admin/health").forStatusCode(200))
            .withMappingFromResource("symbols", "wiremock/supported-currencies.json")
            .withMappingFromResource("rates", "wiremock/exchange-rates.json");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry properties) {
        properties.add("fixer.api.url", () -> CurrencyServiceIntegrationTest.wiremockServer.getBaseUrl());
        properties.add("fixer.api.key", () -> CurrencyServiceIntegrationTest.TEST_KEY);
    }

    @Test
    @Order(1)
    public void testAddCurrency() {
        CurrencyDto firstCurrency = new CurrencyDto();
        firstCurrency.setCode(EUR_CODE);
        firstCurrency.setName(EUR_NAME);
        addCurrency(firstCurrency);

        CurrencyDto secondCurrency = new CurrencyDto();
        secondCurrency.setCode(USD_CODE);
        secondCurrency.setName(USD_NAME);
        addCurrency(secondCurrency);
    }

    @Test
    @Order(2)
    public void testGetSupportedCurrencies() {
        ResponseEntity<CurrencyDto[]> response = restTemplate.getForEntity(createURL(CURRENCIES_URL), CurrencyDto[].class);
        assertEquals(200, response.getStatusCode().value());

        CurrencyDto[] currencies = response.getBody();
        assertNotNull(currencies);
        assertEquals(2, currencies.length);

        CurrencyDto eurCurrency = findCurrencyByCode(currencies, EUR_CODE);
        assertNotNull(eurCurrency);
        assertEquals(EUR_CODE, eurCurrency.getCode());
        assertEquals(EUR_NAME, eurCurrency.getName());

        CurrencyDto usdCurrency = findCurrencyByCode(currencies, USD_CODE);
        assertNotNull(usdCurrency);
        assertEquals(USD_CODE, usdCurrency.getCode());
        assertEquals(USD_NAME, usdCurrency.getName());
    }

    @Test
    @Order(3)
    public void testGetExchangeRate() {
        currencyService.updateExchangeRates();

        ResponseEntity<CurrencyExchangeDto> response = restTemplate.getForEntity(createURL(EXCHANGE_URL), CurrencyExchangeDto.class);

        assertEquals(200, response.getStatusCode().value());
        CurrencyExchangeDto exchangeRate = response.getBody();
        assertNotNull(exchangeRate);
        assertEquals(USD_CODE, exchangeRate.getBase());
        assertNotNull(exchangeRate.getActualizationDate());
        assertNotNull(exchangeRate.getRates());
        assertEquals(1, exchangeRate.getRates().size());

        RateDto rateDto = exchangeRate.getRates().iterator().next();
        assertEquals(EUR_CODE, rateDto.getCurrency());
        assertEquals(BigDecimal.valueOf(0.85), rateDto.getRate());    }

    private void addCurrency(final CurrencyDto currency) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<CurrencyDto> entity = new HttpEntity<>(currency, headers);

        ResponseEntity<CurrencyDto> response = restTemplate.exchange(createURL(CURRENCIES_URL), HttpMethod.POST, entity, CurrencyDto.class);

        assertEquals(201, response.getStatusCode().value());
        CurrencyDto addedCurrency = response.getBody();
        assertNotNull(addedCurrency);
        assertEquals(currency.getCode(), addedCurrency.getCode());
    }

    private String createURL(String uri) {
        return BASE_URL.formatted(port, uri);
    }

    private CurrencyDto findCurrencyByCode(CurrencyDto[] currencies, String code) {
        for (CurrencyDto currency : currencies) {
            if (currency.getCode().equals(code)) {
                return currency;
            }
        }
        return null;
    }
}
package com.xm.trading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xm.trading.entity.BrokerTrade;
import com.xm.trading.model.BrokerTradeDTO;
import lombok.SneakyThrows;
import org.apache.commons.lang.builder.EqualsBuilder;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BuySellControllerIntegrationTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private RestTemplate mockRestTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    private MockRestServiceServer mockServer;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    Random random = new Random();

    public static final List<String> ALLOWABLE_TRADE_SYMBOLS = Arrays.asList("USD/JPY", "EUR/USD");

    @BeforeAll
    public void init() {
        mockServer = MockRestServiceServer.createServer(mockRestTemplate);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    }

    @SneakyThrows
    @Test
    public void whenSellValidTrade_shouldReturnOK() {
        // GIVEN
        BrokerTradeDTO tradeRequest = getRandomBrokerTradeRequest();
        byte[] content = mapper.writeValueAsBytes(tradeRequest);
        HttpEntity<String> entity = new HttpEntity<String>(new String(content), headers);

        // WHEN
        ResponseEntity response = restTemplate.exchange(getURL("/api/sell"), HttpMethod.POST, entity, Object.class, Collections.emptyList());


        // THEN
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());

        String location = response.getHeaders().get("Location").get(0);
        ResponseEntity<BrokerTrade> mvcResultTrade = restTemplate.getForEntity(location.replaceAll("/status", ""), BrokerTrade.class);

        assertEquals(HttpStatus.OK.value(), mvcResultTrade.getStatusCodeValue());
        BrokerTrade brokerTrade = mvcResultTrade.getBody();
        EqualsBuilder.reflectionEquals(tradeRequest, brokerTrade);
    }

    @SneakyThrows
    @Test
    public void whenBuyValidTrade_shouldReturnOK() {
        // GIVEN
        BrokerTradeDTO tradeRequest = getRandomBrokerTradeRequest();
        byte[] content = mapper.writeValueAsBytes(tradeRequest);
        HttpEntity<String> entity = new HttpEntity<String>(new String(content), headers);

        // WHEN
        ResponseEntity response = restTemplate.exchange(getURL("/api/buy"), HttpMethod.POST, entity, Object.class, Collections.emptyList());


        // THEN
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());

        String location = response.getHeaders().get("Location").get(0);
        ResponseEntity<BrokerTrade> mvcResultTrade = restTemplate.getForEntity(location.replaceAll("/status", ""), BrokerTrade.class);

        assertEquals(HttpStatus.OK.value(), mvcResultTrade.getStatusCodeValue());
        BrokerTrade brokerTrade = mvcResultTrade.getBody();
        EqualsBuilder.reflectionEquals(tradeRequest, brokerTrade);
    }

    @SneakyThrows
    @Test
    public void whenBuyInvalidTrade_shouldThrowInvalidQuantityException() {
        // GIVEN
        BrokerTradeDTO tradeRequest = getRandomBrokerTradeRequest();
        tradeRequest.setQuantity(0L);
        byte[] content = mapper.writeValueAsBytes(tradeRequest);
        HttpEntity<String> entity = new HttpEntity<String>(new String(content), headers);

        // WHEN
        ResponseEntity response = restTemplate.exchange(new URI(getURL("/api/buy")), HttpMethod.POST, entity, String.class);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        String errorMessage = response.getBody().toString();
        assertEquals(true, errorMessage.contains("quantity must be greater than 0 and less than or equal to 1M"));
    }

    @SneakyThrows
    @Test
    public void whenBuyInvalidTrade_shouldThrowInvalidPriceException() {
        // GIVEN
        BrokerTradeDTO tradeRequest = getRandomBrokerTradeRequest();
        tradeRequest.setPrice(BigDecimal.valueOf(-1.5D));
        byte[] content = mapper.writeValueAsBytes(tradeRequest);
        HttpEntity<String> entity = new HttpEntity<String>(new String(content), headers);

        // WHEN
        ResponseEntity response = restTemplate.exchange(new URI(getURL("/api/buy")), HttpMethod.POST, entity, String.class);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        String errorMessage = response.getBody().toString();
        assertEquals(true, errorMessage.contains("price must be greater than 0"));
    }

    @SneakyThrows
    @Test
    public void whenBuyInvalidTrade_shouldThrowInvalidSymbolException() {
        // GIVEN
        BrokerTradeDTO tradeRequest = getRandomBrokerTradeRequest();
        tradeRequest.setSymbol("Invalid Symbol");
        byte[] content = mapper.writeValueAsBytes(tradeRequest);
        HttpEntity<String> entity = new HttpEntity<String>(new String(content), headers);

        // WHEN
        ResponseEntity response = restTemplate.exchange(new URI(getURL("/api/buy")), HttpMethod.POST, entity, String.class);

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        String errorMessage = response.getBody().toString();
        assertEquals(true, errorMessage.contains("Symbol valid values"));
    }

    private BrokerTradeDTO getRandomBrokerTradeRequest() {
        BrokerTradeDTO trade = new BrokerTradeDTO();
        int randomCurrencyIndex = random.nextInt( 2);
        trade.setSymbol(ALLOWABLE_TRADE_SYMBOLS.get(randomCurrencyIndex));
        double price = random.nextDouble() % 10D;
        BigDecimal decimal = new BigDecimal(price).setScale(3, RoundingMode.HALF_UP).abs();
        trade.setPrice(decimal);
        long quantity = (random.nextLong() + 1) % 1000000;
        trade.setQuantity(Math.abs(quantity));
        return trade;
    }

    private String getURL(String path) {
        return String.format("http://localhost:%s%s", port, path);
    }

}

package com.xm.trading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xm.trading.entity.BrokerTrade;
import com.xm.trading.enums.TradeStatus;
import com.xm.trading.model.BrokerTradeDTO;
import com.xm.trading.model.TradeStatusDTO;
import com.xm.trading.repository.BrokerTradeRepository;
import lombok.SneakyThrows;
import org.apache.commons.lang.builder.EqualsBuilder;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TradesControllerIntegrationTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private RestTemplate mockRestTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    private MockRestServiceServer mockServer;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    Random random = new Random();

    @Autowired
    BrokerTradeRepository repository;

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
    public void whenBuyValidTradeAndGetItsStatus_shouldReturnPendingExecution() {
        // GIVEN
        BrokerTradeDTO tradeRequest = getRandomBrokerTradeRequest();
        byte[] content = mapper.writeValueAsBytes(tradeRequest);
        HttpEntity<String> entity = new HttpEntity<String>(new String(content), headers);

        // WHEN
        ResponseEntity response = restTemplate.exchange(getURL("/api/buy"), HttpMethod.POST, entity, Object.class, Collections.emptyList());


        // THEN
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCodeValue());

        String location = response.getHeaders().get("Location").get(0);
        ResponseEntity<TradeStatusDTO> mvcResultTrade = restTemplate.getForEntity(location, TradeStatusDTO.class);

        assertEquals(HttpStatus.OK.value(), mvcResultTrade.getStatusCodeValue());
        TradeStatusDTO tradeStatus = mvcResultTrade.getBody();
        assertEquals(new TradeStatusDTO(TradeStatus.PENDING_EXECUTION), tradeStatus);
    }

    @SneakyThrows
    @Test
    public void whenSellValidTradeAndGetItsDetails_shouldReturnTradeDetails() {
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
    public void whenDoingSomeTradesAndGetAllTrades_shouldReturnAllTradesDetails() {
        try {
            Thread.sleep(2 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // GIVEN
        repository.deleteAll();
        int numberOfTradesToMake = 300;
        for (int i = 0; i < numberOfTradesToMake; i++) {
            whenSellValidTrade_shouldReturnOK();
        }
        BrokerTradeDTO tradeRequest = getRandomBrokerTradeRequest();
        byte[] content = mapper.writeValueAsBytes(tradeRequest);
        HttpEntity<String> entity = new HttpEntity<String>(new String(content), headers);

        // WHEN
        AtomicReference<ResponseEntity> response = new AtomicReference<>(restTemplate.exchange(getURL("/api/trades"), HttpMethod.GET, entity, new ParameterizedTypeReference<List<BrokerTrade>>() {}, Collections.emptyList()));


        // THEN
        assertEquals(HttpStatus.OK.value(), response.get().getStatusCodeValue());
        AtomicReference<List<BrokerTrade>> trades = new AtomicReference<>((List<BrokerTrade>) response.get().getBody());
        assertFalse(trades.get().isEmpty());
        assertEquals(numberOfTradesToMake, trades.get().size());

        AtomicBoolean containsNotExecutedTrades = new AtomicBoolean(trades.get().stream().anyMatch(brokerTrade -> brokerTrade.getStatus().equals(TradeStatus.NOT_EXECUTED)));
        AtomicBoolean containsExecutedTrades = new AtomicBoolean(trades.get().stream().anyMatch(brokerTrade -> brokerTrade.getStatus().equals(TradeStatus.EXECUTED)));
        AtomicBoolean containsPendingTrades = new AtomicBoolean(trades.get().stream().anyMatch(brokerTrade -> brokerTrade.getStatus().equals(TradeStatus.PENDING_EXECUTION)));
        AtomicBoolean containsExpiredTrades = new AtomicBoolean(trades.get().stream().anyMatch(brokerTrade -> brokerTrade.getStatus().equals(TradeStatus.NOT_EXECUTED) && brokerTrade.getReason().equalsIgnoreCase("trade expired")));

        assertTrue(containsNotExecutedTrades.get());
        assertTrue(containsExecutedTrades.get());
        assertTrue(containsPendingTrades.get());
        assertFalse(containsExpiredTrades.get());

        RunnableFuture<Void> tradeExpired = new RunnableFuture<Void>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Void get() throws InterruptedException, ExecutionException {
                return null;
            }

            @Override
            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }

            @Override
            public void run() {
                try {
                    Thread.sleep(61 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                response.set(restTemplate.exchange(getURL("/api/trades"), HttpMethod.GET, entity, new ParameterizedTypeReference<List<BrokerTrade>>() {}, Collections.emptyList()));


                // THEN
                assertEquals(HttpStatus.OK.value(), response.get().getStatusCodeValue());
                trades.set((List<BrokerTrade>) response.get().getBody());
                assertFalse(trades.get().isEmpty());
                assertEquals(numberOfTradesToMake, trades.get().size());

                containsNotExecutedTrades.set(trades.get().stream().anyMatch(brokerTrade -> brokerTrade.getStatus().equals(TradeStatus.NOT_EXECUTED)));
                containsExecutedTrades.set(trades.get().stream().anyMatch(brokerTrade -> brokerTrade.getStatus().equals(TradeStatus.EXECUTED)));
                containsPendingTrades.set(trades.get().stream().anyMatch(brokerTrade -> brokerTrade.getStatus().equals(TradeStatus.PENDING_EXECUTION)));
                containsExpiredTrades.set(trades.get().stream().anyMatch(brokerTrade -> brokerTrade.getStatus().equals(TradeStatus.NOT_EXECUTED) && brokerTrade.getReason().equalsIgnoreCase("trade expired")));

                assertTrue(containsNotExecutedTrades.get());
                assertTrue(containsExecutedTrades.get());
                assertFalse(containsPendingTrades.get());
                assertTrue(containsExpiredTrades.get());
            }
        };
        tradeExpired.run();
//        tradeExpired.get();
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

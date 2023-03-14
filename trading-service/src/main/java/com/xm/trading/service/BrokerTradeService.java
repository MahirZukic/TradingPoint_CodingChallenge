package com.xm.trading.service;

import com.xm.trading.entity.BrokerTrade;
import com.xm.trading.enums.BrokerTradeSide;
import com.xm.trading.enums.TradeStatus;
import com.xm.trading.exception.InvalidPriceException;
import com.xm.trading.exception.InvalidQuantityException;
import com.xm.trading.exception.InvalidSymbolException;
import com.xm.trading.external.ExternalBroker;
import com.xm.trading.model.BrokerTradeDTO;
import com.xm.trading.model.TradeStatusDTO;
import com.xm.trading.repository.BrokerTradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrokerTradeService {

    public static final List<String> ALLOWABLE_TRADE_SYMBOLS = Arrays.asList("USD/JPY", "EUR/USD");
    public static final String EXECUTED_OR_PENDING_REASON = null;
    public static final int MILLION_UNITS = 1000000;
    private final BrokerTradeRepository repository;
    private final ExternalBroker broker;

    public List<BrokerTrade> getAllTrades() {
        log.info("Getting all trades.");
        return repository.findAll();
    }

    public BrokerTrade getDetails(UUID tradeId) {
        log.info("Getting trade by ID: {}", tradeId);
        return repository.findBrokerTradeById(tradeId);
    }

    public TradeStatusDTO getStatus(UUID tradeId) {
        return TradeStatusDTO.from(repository.findBrokerTradeById(tradeId).getStatus());
    }

    public BrokerTrade makeTrade(BrokerTradeDTO brokerTrade, BrokerTradeSide side) {
        log.info("Executing a trade {}", brokerTrade);
        if (!ALLOWABLE_TRADE_SYMBOLS.contains(brokerTrade.getSymbol())) {
            InvalidSymbolException exception = new InvalidSymbolException(String.format("Symbol valid values: %s", Arrays.toString(ALLOWABLE_TRADE_SYMBOLS.toArray())));
            log.error("Unable to execute the trade.", exception);
            throw exception;
        }
        if (brokerTrade.getQuantity() <= 0 || brokerTrade.getQuantity() > MILLION_UNITS) {
            InvalidQuantityException exception = new InvalidQuantityException("quantity must be greater than 0 and less than or equal to 1M");
            log.error("Unable to execute the trade.", exception);
            throw exception;
        }
        if (brokerTrade.getPrice().signum() < 0) {
            InvalidPriceException exception = new InvalidPriceException("price must be greater than 0");
            log.error("Unable to execute the trade.", exception);
            throw exception;
        }
        BrokerTrade trade = repository.saveAndFlush(getBrokerTradeFromDTO(brokerTrade, side));
        broker.execute(trade);
        return trade;
    }

    private static BrokerTrade getBrokerTradeFromDTO(BrokerTradeDTO brokerTrade, BrokerTradeSide side) {
        return new BrokerTrade(UUID.randomUUID(), brokerTrade.getSymbol(), brokerTrade.getQuantity(), side, TradeStatus.PENDING_EXECUTION, brokerTrade.getPrice(), EXECUTED_OR_PENDING_REASON, LocalDateTime.now());
    }
}

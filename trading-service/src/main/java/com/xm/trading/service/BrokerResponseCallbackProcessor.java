package com.xm.trading.service;

import com.xm.trading.entity.BrokerTrade;
import com.xm.trading.enums.TradeStatus;
import com.xm.trading.external.BrokerResponseCallback;
import com.xm.trading.repository.BrokerTradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerResponseCallbackProcessor implements BrokerResponseCallback {

    public static final String EXECUTED_TRADE_REASON = null;
    private final BrokerTradeRepository repository;

    public void successful(final UUID tradeId) {
        BrokerTrade trade = repository.findBrokerTradeById(tradeId);
        trade.setStatus(TradeStatus.EXECUTED);
        trade.setReason(EXECUTED_TRADE_REASON);
        repository.saveAndFlush(trade);
        log.info("Trade ID: [{}] succesfully executed.", tradeId);
    }

    public void unsuccessful(final UUID tradeId, final String reason) {
        BrokerTrade trade = repository.findBrokerTradeById(tradeId);
        trade.setStatus(TradeStatus.NOT_EXECUTED);
        trade.setReason(reason);
        repository.saveAndFlush(trade);
        log.info("Trade ID: [{}] failed to execute due to {}", tradeId, reason);
    }
}

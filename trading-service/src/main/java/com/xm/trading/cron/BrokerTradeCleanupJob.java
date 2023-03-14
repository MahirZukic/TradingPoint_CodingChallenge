package com.xm.trading.cron;

import com.xm.trading.entity.BrokerTrade;
import com.xm.trading.enums.TradeStatus;
import com.xm.trading.repository.BrokerTradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class BrokerTradeCleanupJob {

    public static final String TRADE_EXPIRED = "trade expired";
    private final BrokerTradeRepository repository;

    public static final long SECONDS_IN_A_MINUTE = 60;
    public static final int MILLISECONDS_IN_SECOND = 1000;

    @Scheduled(fixedRate = MILLISECONDS_IN_SECOND * SECONDS_IN_A_MINUTE, initialDelay = 0)
    public void cleanUpStaleTrades() {
        try {
            log.info("Executing cleanup job of stale trades.");
            LocalDateTime timeOfStaleness = LocalDateTime.now().minusSeconds(30);
            List<BrokerTrade> staleBrokerTrades = repository.findAllByStatusAndTimestampBefore(TradeStatus.PENDING_EXECUTION, timeOfStaleness);
            for (BrokerTrade staleBrokerTrade : staleBrokerTrades) {
                staleBrokerTrade.setStatus(TradeStatus.NOT_EXECUTED);
                staleBrokerTrade.setReason(TRADE_EXPIRED);
            }
            repository.saveAll(staleBrokerTrades);

            log.info("Successfully cleaned up stale trades.");
        } catch (Exception e) {
            log.error("Error occurred while cleaning up the stale trades. Will try to clean up again in {} seconds!", SECONDS_IN_A_MINUTE, e);
        }
    }
}

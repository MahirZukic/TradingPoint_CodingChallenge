package com.xm.trading.external;

import com.xm.trading.entity.BrokerTrade;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class ExternalBroker {
    private final Random random = new Random();
    private final BrokerResponseCallback callback;

    public ExternalBroker(BrokerResponseCallback callback) {
        this.callback = callback;
    }

    public void execute(final BrokerTrade trade) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(random.nextInt(2000));
                final int result = random.nextInt(3);
                if (result == 0) {
                    callback.successful(trade.getId());
                } else if (result == 1) {
                    callback.unsuccessful(trade.getId(), "No available quotes");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

}

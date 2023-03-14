package com.xm.trading.repository;

import com.xm.trading.entity.BrokerTrade;
import com.xm.trading.enums.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BrokerTradeRepository extends JpaRepository<BrokerTrade, UUID> {

    BrokerTrade findBrokerTradeById(UUID uuid);

    List<BrokerTrade> findAllByStatusAndTimestampBefore(TradeStatus status, LocalDateTime time);

}

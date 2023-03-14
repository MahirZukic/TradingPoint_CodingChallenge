package com.xm.trading.entity;

import com.xm.trading.enums.BrokerTradeSide;
import com.xm.trading.enums.TradeStatus;
import lombok.*;

//import javax.persistence.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity(name = "broker_trade")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class BrokerTrade {


    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "quantity")
    private long quantity;
    @Column(name = "side")
    @Enumerated(EnumType.ORDINAL)
    private BrokerTradeSide side;
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private TradeStatus status;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "reason")
    private String reason;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

}

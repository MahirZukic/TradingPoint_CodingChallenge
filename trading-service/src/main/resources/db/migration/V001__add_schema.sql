CREATE TABLE IF NOT EXISTS `broker_trade` (
	`id` uuid NOT NULL PRIMARY KEY,
	`symbol` varchar(50) NOT NULL,
	`quantity` bigint NOT NULL,
	`side` integer NOT NULL,
	`status` integer NOT NULL,
	`price` numeric NOT NULL,
	`reason` varchar NULL,
	`timestamp` timestamp NOT NULL default now()
);


-- id;
-- private final String symbol;
-- private final long quantity;
-- private final BrokerTradeSide side;
-- private final BigDecimal price;
-- private final String reason;
-- private final LocalDateTime timestamp;

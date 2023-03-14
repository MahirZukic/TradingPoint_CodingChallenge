## Content

### Building application
Building and testing application could be done by
```shell
mvn clean verify
```
or 
```
mvnw clean verify - in case there is no maven installed locally, maven wrapper (mvnw program) will do
```

### Running the applications
```
.
├── eureka
    Default Port: 8761
    To start use: mvn spring-boot:run (inside of eureka directory)
└── gateway
    Default Port: 9080
    To start use: mvn spring-boot:run (inside of gateway directory)
└── trading-service
    Default Port: 9180
    To start use: mvn spring-boot:run (inside of trading-service directory)

```
Run them in the order listed here to ensure everything goes smoothly.

#### create a trade order
curl --location --request POST "http://localhost:9180/api/buy" \
--header "Accept: application/json" \
--header "Content-Type: application/json" \
--data "{
\"symbol\": "EUR/USD",
\"quantity\": 1000,
\"price\": 1.123
}" --verbose

#### retrieve a trade order
curl --location --request GET "http://localhost:9180/api/trades/${TRADE_ID}" \
--header "Accept: application/json" \
--header "Content-Type: application/json" \

#### retrieve a trade order status
curl --location --request GET "http://localhost:9180/api/trades/${TRADE_ID}/status" \
--header "Accept: application/json" \
--header "Content-Type: application/json" \

#### retrieve all trade orders
curl --location --request GET "http://localhost:9180/api/trades" \
--header "Accept: application/json" \
--header "Content-Type: application/json" \

Since this coding challenge was made using microservice architecture, we can use one component of it here in the following fashion:
* instead of making these calls above by accessing the `trading-service` directly, we can access it alternatively 
* by replacing the urls above with `http://localhost:9080/trading-service/${API_CALL}`
  * so http://localhost:9180/api/buy becomes http://localhost:9080/trading-service/api/buy
  * so http://localhost:9180/api/sell becomes http://localhost:9080/trading-service/api/sell
  * so http://localhost:9180/api/trades/${TRADE_ID} becomes http://localhost:9080/trading-service/api/trades/${TRADE_ID}
  * so http://localhost:9180/api/trades/${TRADE_ID}/status becomes http://localhost:9080/trading-service/api/trades/${TRADE_ID}/status
  * so http://localhost:9180/api/trades becomes http://localhost:9080/trading-service/api/trades
* Running these alternative urls will route our api call through the gateway which would then route it to the `trading-service` microservice and execute the call 

### Additional notes:
All of this could be deployed via Dockerfile and inside of docker containers, and even a `docker-compose` file specifiying the dependencies on these micorservices,
but due to time limitation, this was omitted.

Additionally, the observability can be enabled via uncommenting the dependencies in `pom.xml` files of the respective microservices,
as well as setting the required properties in the `application.yml` file, either directly or via cloud config solution.
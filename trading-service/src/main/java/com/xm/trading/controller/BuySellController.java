package com.xm.trading.controller;

import com.xm.trading.entity.BrokerTrade;
import com.xm.trading.enums.BrokerTradeSide;
import com.xm.trading.model.BrokerTradeDTO;
import com.xm.trading.service.BrokerTradeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequiredArgsConstructor
public class BuySellController {

    private final BrokerTradeService brokerTradeService;

    @SneakyThrows
    @ApiOperation(value = "Make a SELL trade")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created a SELL trade order"),
            @ApiResponse(code = 400, message = "Error creating a SELL trade order"),
    })
    @PostMapping(name = "/api/sell", value = "/api/sell", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity sell(@ApiParam(value = "Customer Id used to search loans", required = true) @RequestBody @Valid BrokerTradeDTO brokerTrade,
                               HttpServletRequest request) {
        BrokerTrade trade = brokerTradeService.makeTrade(brokerTrade, BrokerTradeSide.SELL);
        URI location = getLocation(request, "/sell", trade);
        return ResponseEntity.created(location).build();
    }
    
    @SneakyThrows
    @ApiOperation(value = "Make a BUY trade")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created a BUY trade order"),
            @ApiResponse(code = 400, message = "Error creating a BUY trade order"),
    })
    @PostMapping(name = "/api/buy", value = "/api/buy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity buy(@ApiParam(value = "Loan object to be stored", required = true) @RequestBody @Valid BrokerTradeDTO brokerTrade,
                              HttpServletRequest request) {
        BrokerTrade trade = brokerTradeService.makeTrade(brokerTrade, BrokerTradeSide.BUY);
        URI location = getLocation(request, "/buy", trade);
        return ResponseEntity.created(location).build();
    }

    private URI getLocation(HttpServletRequest request, String regex, BrokerTrade trade) throws URISyntaxException {
        String requestUrl = request.getRequestURL().toString();
        URI location = new URI(requestUrl.replaceAll(regex, "/trades/") + trade.getId() + "/status");
        return location;
    }
}

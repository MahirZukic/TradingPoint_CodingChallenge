package com.xm.trading.controller;

import com.xm.trading.entity.BrokerTrade;
import com.xm.trading.model.TradeStatusDTO;
import com.xm.trading.service.BrokerTradeService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
@Api(value="Loan Applications API")
public class TradesController {

    private final BrokerTradeService brokerTradeService;

    @ApiOperation(value = "Search all trades in the system", response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved all the trades"),
            @ApiResponse(code = 400, message = "Error searching for trades"),
            @ApiResponse(code = 404, message = "Customer not found")
    })
    @GetMapping
    public ResponseEntity<List<BrokerTrade>> getAllTrades() {
        return ResponseEntity.ok().body(brokerTradeService.getAllTrades());
    }
    
    @ApiOperation(value = "Get details of a Trade", response = BrokerTrade.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully gotten details of a Trade"),
            @ApiResponse(code = 404, message = "Trade not found")
    })
    @GetMapping("/{tradeId}")
    public ResponseEntity<BrokerTrade> getTradeDetails(@ApiParam(value = "Trade's ID to be retrieved", required = true) @PathVariable(name = "tradeId") @Valid UUID tradeId) {
        return ResponseEntity.ok().body(brokerTradeService.getDetails(tradeId));
    }

    @ApiOperation(value = "Get a status of a Trade", response = TradeStatusDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully gotten status of a Trade"),
            @ApiResponse(code = 404, message = "Trade not found")
    })
    @GetMapping("/{tradeId}/status")
    public ResponseEntity<TradeStatusDTO> getTradeStatus(@ApiParam(value = "Trade's ID to be retrieved", required = true) @PathVariable(name = "tradeId") @Valid UUID tradeId) {
        return ResponseEntity.ok().body(brokerTradeService.getStatus(tradeId));
    }
}

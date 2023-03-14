package com.xm.trading.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xm.trading.enums.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeStatusDTO {
    private TradeStatus status;

    public static TradeStatusDTO from(TradeStatus status) {
        return new TradeStatusDTO(status);
    }
}

package com.xm.trading.advice;

import com.xm.trading.controller.BuySellController;
import com.xm.trading.exception.InvalidPriceException;
import com.xm.trading.exception.InvalidQuantityException;
import com.xm.trading.exception.InvalidSymbolException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = BuySellController.class)
public class BuySellControllerExceptionHandler {

    @ExceptionHandler(InvalidSymbolException.class)
    public ResponseEntity<String> handleException(InvalidSymbolException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<String> handleException(InvalidQuantityException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(InvalidPriceException.class)
    public ResponseEntity<String> handleException(InvalidPriceException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
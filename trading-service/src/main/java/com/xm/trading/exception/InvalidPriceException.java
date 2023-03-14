package com.xm.trading.exception;

public class InvalidPriceException extends RuntimeException {
    public InvalidPriceException(String errorMessage) {
        super(errorMessage);
    }
}

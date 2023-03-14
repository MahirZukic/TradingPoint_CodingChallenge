package com.xm.trading.exception;

public class InvalidSymbolException extends RuntimeException {
    public InvalidSymbolException(String errorMessage) {
        super(errorMessage);
    }
}

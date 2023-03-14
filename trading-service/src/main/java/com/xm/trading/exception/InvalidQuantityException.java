package com.xm.trading.exception;

public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(String errorMessage) {
        super(errorMessage);
    }
}

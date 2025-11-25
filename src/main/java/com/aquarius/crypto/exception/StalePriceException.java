package com.aquarius.crypto.exception;

public class StalePriceException extends RuntimeException {
    public StalePriceException(String message) {
        super(message);
    }
}

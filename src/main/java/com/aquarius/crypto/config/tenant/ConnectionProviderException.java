package com.aquarius.crypto.config.tenant;

public class ConnectionProviderException extends RuntimeException {

    public ConnectionProviderException(String message) {
        super(message);
    }

    public ConnectionProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
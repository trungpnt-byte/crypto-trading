package com.aquarius.crypto.dto;

public enum TradeType {
    BUY,
    SELL;

    public static TradeType from(String input) {
        if (input == null) {
            return SELL;
        }
        return switch (input.trim().toUpperCase()) {
            case "BUY" -> BUY;
            case "SELL" -> SELL;
            default -> SELL;
        };
    }
}

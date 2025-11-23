package com.aquarius.crypto.common;

public class ExtractionHelper {
    public static String extractTokenValue(String headerValue) {
        if (headerValue == null || headerValue.isEmpty()) {
            return null;
        }
        return LocalStringUtils.stripUntil(headerValue, Character::isWhitespace);
    }
}

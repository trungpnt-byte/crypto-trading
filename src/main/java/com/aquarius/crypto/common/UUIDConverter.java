package com.aquarius.crypto.common;

import java.util.UUID;

public class UUIDConverter {

    public static String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    public static boolean reverseParseUUID(UUID uuid) {
        return tryParseUUID(uuid.toString());
    }

    public static UUID stringToUuid(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }
    }

    private static boolean tryParseUUID(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }
    }
}

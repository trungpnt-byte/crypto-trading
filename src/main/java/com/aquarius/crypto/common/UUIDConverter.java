package com.aquarius.crypto.common;

import java.util.UUID;

public class UUIDConverter {

    public static String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    public static UUID stringToUuid(String uuidString) {
        try {
            return uuidString != null ? UUID.fromString(uuidString) : null;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }
    }
}

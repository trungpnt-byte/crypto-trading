package com.aquarius.crypto.common;

import io.jsonwebtoken.MalformedJwtException;

public class UUIDHelper {
    public static void ensureValid(String publicIdStr) {
        try {
            UUIDConverter.stringToUuid(publicIdStr);
        } catch (IllegalArgumentException e) {
            throw new MalformedJwtException("Claim 'user_public_id' is not a valid UUID format.");
        }
    }
}

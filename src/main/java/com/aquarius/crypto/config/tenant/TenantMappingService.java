package com.aquarius.crypto.config.tenant;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class TenantMappingService {

    // prevent SQL injection/routing attacks
    private static final Map<String, String> SCHEMA_MAP = Map.of(
            "trader", "trader",
            "admin", "admin",
            "broker", "broker",
            "public", "public"
    );

    public Optional<String> mapJwtTenantToSchema(String jwtTenantClaim) {
        return Optional.ofNullable(SCHEMA_MAP.get(jwtTenantClaim.toLowerCase()));
    }
}

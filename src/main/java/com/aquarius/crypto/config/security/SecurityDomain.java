package com.aquarius.crypto.config.security;

import com.aquarius.crypto.config.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class SecurityDomain {
    public String getTenantIdFromJwt(HttpServletRequest req) {
        //TODO implement AAA
        return TenantContext.REVIT_TENANT_ID;
    }
}
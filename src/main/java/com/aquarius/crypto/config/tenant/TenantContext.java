package com.aquarius.crypto.config.tenant;


public final class TenantContext {

    public static final String DEFAULT_TENANT_ID = "public";
    public static final String REVIT_TENANT_ID = "revit";

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentTenant(String tenant) {
        if (tenant != null && tenant.equals(currentTenant.get())) {
            return;
        }
        currentTenant.set(tenant);
    }

    public static String getCurrentTenant() {
        String tenant = currentTenant.get();
        return (tenant != null) ? tenant : REVIT_TENANT_ID;
    }

    public static void clear() {
        currentTenant.remove();
    }
}
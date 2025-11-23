package com.aquarius.crypto.config.tenant;


public final class TenantContext {

    public static final String DEFAULT_TENANT_ID = "public";
    public static final String TRADER_TENANT_ID = "trader";

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    private TenantContext() {
    }

    public static String getCurrentTenant() {
        String tenant = currentTenant.get();
        return (tenant != null) ? tenant : TRADER_TENANT_ID;
    }

    public static void setCurrentTenant(String tenant) {
        if (tenant != null && tenant.equals(currentTenant.get())) {
            return;
        }
        currentTenant.set(tenant);
    }

    public static void clear() {
        currentTenant.remove();
    }
}
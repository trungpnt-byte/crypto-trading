package com.aquarius.crypto.config.tenant;

import reactor.util.context.ContextView;

public final class TenantContext {


    public static final String TENANT_ID_KEY = "currentTenant";

    public static final String DEFAULT_TENANT_ID = "public";


    public static final String TRADER_TENANT_ID = "trader";

    private TenantContext() {
    }


    public static String getTenantId(ContextView contextView) {

        String tenant = contextView.getOrDefault(TENANT_ID_KEY, TRADER_TENANT_ID);


        return (tenant != null) ? tenant : TRADER_TENANT_ID;
    }
}
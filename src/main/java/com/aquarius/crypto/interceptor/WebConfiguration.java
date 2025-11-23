package com.aquarius.crypto.interceptor;

import com.aquarius.crypto.config.tenant.TenantRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Autowired
    private TenantRequestInterceptor tenantRequestInterceptor;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(tenantRequestInterceptor).addPathPatterns("/**");
    }
}

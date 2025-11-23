package com.aquarius.crypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.aquarius")
public class CryptoTradingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoTradingApplication.class, args);
    }

}

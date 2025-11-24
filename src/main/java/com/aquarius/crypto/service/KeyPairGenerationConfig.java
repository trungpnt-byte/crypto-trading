package com.aquarius.crypto.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Configuration
@Slf4j
public class KeyPairGenerationConfig {

    @Bean
    public KeyPair jwtSigningKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.generateKeyPair();
            
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            String encodedPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            log.warn("--- ALGORITHM: RSA 2048 ---");
            log.warn("--- PUBLIC KEY (FOR VERIFICATION): {}... ---", encodedPublicKey.substring(0, 50));

            return keyPair;

        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate RSA key pair", e);
            throw new IllegalStateException("RSA key generation failed.", e);
        }
    }

    @Bean
    public RSAPublicKey jwtPublicKey(KeyPair keyPair) {
        return (RSAPublicKey) keyPair.getPublic();
    }

    @Bean
    public RSAPrivateKey jwtPrivateKey(KeyPair keyPair) {
        return (RSAPrivateKey) keyPair.getPrivate();
    }
}
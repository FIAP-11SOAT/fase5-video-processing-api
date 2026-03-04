package com.example.demo.shared.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;


@Configuration
public class JwtConfig {

    @Value("${fase5.notification.worker.cognito_jwk_url}")
    private String jwkUrl;

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            return NimbusJwtDecoder.withJwkSetUri(jwkUrl).build();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao carregar ou parsear a chave pública JWK", e);
        }
    }
}
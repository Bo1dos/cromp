package com.cromp.iam.infrastructure.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "iam.jwt")
public class JwtConfig {
    //TODO: Нужна конфигурация для секрета и expiration. В application.yml добавить надо бы или еще куда
    private String secret = "changeit-changeit-changeit-changeit"; // минимум 256 бит
    private long expirationMs = 3600_000; // 1 час
}
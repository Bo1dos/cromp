package com.cromp.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Точка входа модульного монолита "Cron as a Service".
 *
 * Orchestrator — process-модуль: не содержит собственной доменной модели,
 * только координирует работу модулей iam, jobs, schedules, executions, secrets
 * через их публичные порты.
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.cromp.common",
                "com.cromp.iam",
                "com.cromp.jobs",
                "com.cromp.schedules",
                "com.cromp.executions",
                "com.cromp.secrets",
                "com.cromp.analytics",
                "com.cromp.orchestrator"
        }
)
@EnableJpaRepositories(basePackages = "com.cromp")
@EntityScan(basePackages = "com.cromp")
@EnableScheduling
@EnableConfigurationProperties
public class OrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
    }
}
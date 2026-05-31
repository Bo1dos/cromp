package com.cromp.orchestrator.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger конфигурация для Cron as a Service.
 *
 * Определяет:
 * - метаданные API,
 * - JWT Bearer security scheme,
 * - группировку endpoints по модулям через теги.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cron as a Service API",
                description = """
                        REST API для платформы Cron as a Service — модульного монолита
                        для управления периодическими задачами (cron jobs).

                        Модули:
                        - **IAM** — аутентификация, пользователи, организации, роли
                        - **Jobs** — CRUD задач, версионирование, ручной запуск
                        - **Schedules** — cron-расписания для задач
                        - **Secrets** — безопасное хранение секретов
                        - **Executions** — история запусков, попытки, артефакты
                        - **Analytics** — аналитика, прогнозы, аномалии
                        """,
                version = "1.0.0",
                contact = @Contact(
                        name = "Cromp Team",
                        email = "dev@cromp.io"
                ),
                license = @License(
                        name = "Proprietary",
                        url = "https://cromp.io/license"
                )
        ),
        servers = {
                @Server(url = "${cromp.api.server-url:http://localhost:8080}", description = "Локальный / дефолтный сервер")
        },
        security = @SecurityRequirement(name = "bearerAuth"),
        tags = {
                @Tag(name = "Auth", description = "Регистрация, логин, выбор организации"),
                @Tag(name = "Users", description = "Управление профилями пользователей"),
                @Tag(name = "Organizations", description = "Управление организациями"),
                @Tag(name = "Memberships", description = "Управление членством в организациях"),
                @Tag(name = "Invitations", description = "Приглашения в организации"),
                @Tag(name = "Roles & Permissions", description = "Управление ролями и разрешениями"),
                @Tag(name = "Audit", description = "Журнал аудита"),
                @Tag(name = "Jobs", description = "Управление задачами (cron jobs)"),
                @Tag(name = "Schedules", description = "Управление расписаниями задач"),
                @Tag(name = "Secrets", description = "Управление секретами"),
                @Tag(name = "Executions", description = "История запусков и попытки"),
                @Tag(name = "Analytics", description = "Аналитика выполнения задач")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token, полученный через POST /api/v1/auth/login"
)
public class OpenApiConfig {

    /**
     * Группировка для Swagger UI: все публичные endpoints.
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("cromp-api")
                .displayName("Cron as a Service — All Endpoints")
                .pathsToMatch("/api/**")
                .build();
    }
}

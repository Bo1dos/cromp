package com.cromp.secrets.infrastructure.web.controller;

import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;
import com.cromp.secrets.api.service.SecretFacade;
import com.cromp.iam.application.port.PermissionCheckerPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SecretControllerTest.TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(SecretControllerTest.TestSecurityConfig.class)
class SecretControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @org.springframework.boot.test.mock.mockito.MockBean
    private SecretFacade secretFacade;
    @org.springframework.boot.test.mock.mockito.MockBean(name = "permissionCheckerPort")
    private PermissionCheckerPort permissionCheckerPort;

    @Test
    void shouldCreateSecretWhenRequestIsValidAndPermissionExists() throws Exception {
        UUID secretUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "secret:create")).thenReturn(true);
        when(secretFacade.createSecret(any())).thenReturn(secretResponse(secretUuid, 1));

        mockMvc.perform(post("/api/v1/secrets")
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSecretRequest("api-key", "value", null, "description"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.secretUuid").value(secretUuid.toString()))
                .andExpect(jsonPath("$.name").value("api-key"))
                .andExpect(jsonPath("$.scope").value("JOB"))
                .andExpect(jsonPath("$.currentVersion").value(1));

        verify(secretFacade).createSecret(any());
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/secrets")
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSecretRequest("", "", null, null))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(secretFacade);
    }

    @Test
    void shouldReturnUnauthorizedWhenRequestIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/secrets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenCreatePermissionIsMissing() throws Exception {
        when(permissionCheckerPort.hasPermission(11L, "secret:create")).thenReturn(false);

        mockMvc.perform(post("/api/v1/secrets")
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSecretRequest("api-key", "value", null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnSecretsListWhenAuthenticated() throws Exception {
        when(secretFacade.listSecrets()).thenReturn(List.of(secretResponse(UUID.randomUUID(), 1)));

        mockMvc.perform(get("/api/v1/secrets").with(authentication(auth(11L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("api-key"));

        verify(secretFacade).listSecrets();
    }

    @Test
    void shouldReturnSecretWhenUuidPathIsBound() throws Exception {
        UUID secretUuid = UUID.randomUUID();
        when(secretFacade.getSecret(secretUuid)).thenReturn(secretResponse(secretUuid, 3));

        mockMvc.perform(get("/api/v1/secrets/{secretUuid}", secretUuid)
                        .with(authentication(auth(11L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVersion").value(3));

        verify(secretFacade).getSecret(secretUuid);
    }

    @Test
    void shouldDeleteSecretWhenPermissionExists() throws Exception {
        UUID secretUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "secret:delete")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/secrets/{secretUuid}", secretUuid)
                        .with(authentication(auth(11L))))
                .andExpect(status().isNoContent());

        verify(secretFacade).deleteSecret(secretUuid);
    }

    @Test
    void shouldRotateSecretWhenPermissionExists() throws Exception {
        UUID secretUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "secret:update")).thenReturn(true);
        when(secretFacade.rotateSecret(any(), any())).thenReturn(secretResponse(secretUuid, 4));

        mockMvc.perform(post("/api/v1/secrets/{secretUuid}/rotate", secretUuid)
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RotateSecretRequest("new-value"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVersion").value(4));

        verify(secretFacade).rotateSecret(secretUuid, new RotateSecretRequest("new-value"));
    }

    @Test
    void shouldReturnVersionsWhenAuthenticated() throws Exception {
        when(secretFacade.getVersions(any())).thenReturn(List.of(new SecretVersionResponse(1, true, Instant.parse("2024-01-01T00:00:00Z"), null)));

        mockMvc.perform(get("/api/v1/secrets/{secretUuid}/versions", UUID.randomUUID())
                        .with(authentication(auth(11L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].version").value(1));
    }

    private static SecretResponse secretResponse(UUID secretUuid, int currentVersion) {
        return new SecretResponse(
                secretUuid,
                "api-key",
                "JOB",
                "description",
                currentVersion,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private static Authentication auth(Long userId) {
        return UsernamePasswordAuthenticationToken.authenticated(userId, "n/a", List.of());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            LiquibaseAutoConfiguration.class
    })
    @ComponentScan(basePackageClasses = SecretController.class)
    @Import(TestSecurityConfig.class)
    static class TestApplication {
    }

    @org.springframework.context.annotation.Configuration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .anonymous(AbstractHttpConfigurer::disable)
                    .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }
}

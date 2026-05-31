package com.cromp.executions.infrastructure.web.controller;

import com.cromp.executions.api.dto.ExecutionFilter;
import com.cromp.executions.api.dto.request.UploadArtifactRequest;
import com.cromp.executions.api.dto.response.ArtifactResponse;
import com.cromp.executions.api.dto.response.AttemptResponse;
import com.cromp.executions.api.dto.response.ExecutionDetailResponse;
import com.cromp.executions.api.dto.response.ExecutionResponse;
import com.cromp.executions.api.dto.response.PagedResponse;
import com.cromp.executions.application.service.ArtifactService;
import com.cromp.executions.application.service.AttemptService;
import com.cromp.executions.application.service.ExecutionService;
import com.cromp.executions.domain.model.enums.ArtifactKind;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ExecutionControllerTest.TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(ExecutionControllerTest.TestSecurityConfig.class)
class ExecutionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @org.springframework.boot.test.mock.mockito.MockBean private ExecutionService executionService;
    @org.springframework.boot.test.mock.mockito.MockBean private AttemptService attemptService;
    @org.springframework.boot.test.mock.mockito.MockBean private ArtifactService artifactService;
    @org.springframework.boot.test.mock.mockito.MockBean(name = "permissionCheckerPort")
    private PermissionCheckerPort permissionCheckerPort;

    @Test
    void shouldReturnExecutionsListWhenAuthenticatedAndBindFilters() throws Exception {
        UUID execUuid = UUID.randomUUID();
        when(executionService.listExecutions(eq(10L), org.mockito.ArgumentMatchers.any(ExecutionFilter.class))).thenReturn(new PagedResponse<>(
                List.of(executionResponse(execUuid)), 2, 20, 1
        ));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/executions", 10L)
                        .with(authentication(auth(11L)))
                        .param("jobId", "33")
                        .param("status", "CREATED")
                        .param("source", "API")
                        .param("from", "2024-01-01T00:00:00Z")
                        .param("to", "2024-01-02T00:00:00Z")
                        .param("page", "2")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].execUuid").value(execUuid.toString()))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(20));

        verify(executionService).listExecutions(eq(10L), org.mockito.ArgumentMatchers.argThat(filter ->
                filter.jobId().equals(33L)
                        && filter.status() == ExecutionStatus.CREATED
                        && filter.source() == ExecutionSource.API
                        && filter.page() == 2
                        && filter.size() == 20));
    }

    @Test
    void shouldReturnExecutionDetailsWhenAuthenticated() throws Exception {
        UUID execUuid = UUID.randomUUID();
        when(executionService.getExecution(10L, execUuid)).thenReturn(executionDetailResponse(execUuid));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/executions/{executionId}", 10L, execUuid)
                        .with(authentication(auth(11L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.execUuid").value(execUuid.toString()))
                .andExpect(jsonPath("$.attempts[0].status").value("RUNNING"));

        verify(executionService).getExecution(10L, execUuid);
    }

    @Test
    void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{organizationId}/executions", 10L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenCancelPermissionMissing() throws Exception {
        UUID execUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "job:execute")).thenReturn(false);

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/executions/{executionId}/cancel", 10L, execUuid)
                        .with(authentication(auth(11L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNoContentWhenCancelIsAllowed() throws Exception {
        UUID execUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "job:execute")).thenReturn(true);

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/executions/{executionId}/cancel", 10L, execUuid)
                        .with(authentication(auth(11L))))
                .andExpect(status().isNoContent());

        verify(executionService).cancelExecution(10L, execUuid);
    }

    @Test
    void shouldReturnAttemptsWhenAuthenticated() throws Exception {
        UUID execUuid = UUID.randomUUID();
        when(attemptService.listAttempts(10L, execUuid)).thenReturn(List.of(attemptResponse()));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/executions/{executionId}/attempts", 10L, execUuid)
                        .with(authentication(auth(11L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("RUNNING"));

        verify(attemptService).listAttempts(10L, execUuid);
    }

    @Test
    void shouldReturnArtifactsWhenAuthenticated() throws Exception {
        UUID execUuid = UUID.randomUUID();
        when(artifactService.listArtifacts(10L, execUuid)).thenReturn(List.of(artifactResponse()));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/executions/{executionId}/artifacts", 10L, execUuid)
                        .with(authentication(auth(11L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kind").value("LOG_STDOUT"));

        verify(artifactService).listArtifacts(10L, execUuid);
    }

    @Test
    void shouldReturnCreatedWhenUploadingArtifactWithMultipartRequest() throws Exception {
        UUID execUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "job:execute")).thenReturn(true);
        when(artifactService.uploadArtifact(eq(10L), eq(execUuid), org.mockito.ArgumentMatchers.any(UploadArtifactRequest.class), org.mockito.ArgumentMatchers.any(MultipartFile.class))).thenReturn(artifactResponse());

        mockMvc.perform(multipart("/api/v1/organizations/{organizationId}/executions/{executionId}/artifacts", 10L, execUuid)
                        .file(new org.springframework.mock.web.MockMultipartFile("meta", "", MediaType.APPLICATION_JSON_VALUE,
                                objectMapper.writeValueAsBytes(new UploadArtifactRequest(ArtifactKind.LOG_STDOUT, "text/plain", 10))))
                        .file(new org.springframework.mock.web.MockMultipartFile("file", "stdout.log", "text/plain", "hello".getBytes()))
                        .with(authentication(auth(11L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kind").value("LOG_STDOUT"));

        verify(artifactService).uploadArtifact(eq(10L), eq(execUuid), org.mockito.ArgumentMatchers.any(UploadArtifactRequest.class), org.mockito.ArgumentMatchers.any(MultipartFile.class));
    }

    @Test
    void shouldReturnBadRequestWhenUploadRequestPartIsMissing() throws Exception {
        UUID execUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "job:execute")).thenReturn(true);

        mockMvc.perform(multipart("/api/v1/organizations/{organizationId}/executions/{executionId}/artifacts", 10L, execUuid)
                        .file(new org.springframework.mock.web.MockMultipartFile("file", "stdout.log", "text/plain", "hello".getBytes()))
                        .with(authentication(auth(11L))))
                .andExpect(status().isBadRequest());
    }

    private static ExecutionResponse executionResponse(UUID execUuid) {
        return new ExecutionResponse(
                execUuid, 33L, 44L, 5, "API", "CREATED", 1,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"),
                null, null, UUID.randomUUID(), Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private static ExecutionDetailResponse executionDetailResponse(UUID execUuid) {
        return new ExecutionDetailResponse(
                execUuid, 33L, 44L, 5, "API", "RUNNING", 1,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:01:00Z"), null, UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:02:00Z"),
                List.of(attemptResponse())
        );
    }

    private static AttemptResponse attemptResponse() {
        return new AttemptResponse(
                UUID.randomUUID(), 1, "RUNNING", "reason", null,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:01:00Z"),
                Instant.parse("2024-01-01T00:02:00Z"), null, null, UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private static ArtifactResponse artifactResponse() {
        return new ArtifactResponse(
                1L, "LOG_STDOUT", "executions/10/stdout.log", 1L, "text/plain", "checksum", 10, Instant.parse("2024-01-01T00:00:00Z")
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
    @ComponentScan(basePackageClasses = ExecutionController.class)
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
                    .csrf(AbstractHttpConfigurer::disable)
                    .anonymous(AbstractHttpConfigurer::disable)
                    .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }
}

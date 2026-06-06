package com.cromp.jobs.infrastructure.web.controller;

import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.jobs.api.dto.request.*;
import com.cromp.jobs.api.dto.response.*;
import com.cromp.jobs.api.service.JobFacade;
import com.cromp.jobs.api.service.JobVersionFacade;
import com.cromp.jobs.domain.model.JobConfig;
import com.cromp.jobs.domain.model.JobTarget;
import com.cromp.jobs.domain.model.RetryPolicy;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.model.exceptions.InvalidJobStateException;
import com.cromp.jobs.domain.model.exceptions.JobNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import({JobController.class, JobControllerTest.TestSecurityConfig.class, JobControllerTest.TestExceptionAdvice.class})
class JobControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @org.springframework.boot.test.mock.mockito.MockBean private JobFacade jobFacade;
    @org.springframework.boot.test.mock.mockito.MockBean private JobVersionFacade jobVersionFacade;
    @org.springframework.boot.test.mock.mockito.MockBean(name = "permissionCheckerPort")
    private PermissionCheckerPort permissionCheckerPort;

    @Test
    void shouldCreateJobWhenRequestIsValid() throws Exception {
        UUID jobUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "job:create")).thenReturn(true);
        when(jobFacade.createJob(any())).thenReturn(jobResponse(jobUuid, 1));

        mockMvc.perform(post("/api/v1/jobs")
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateJobRequest("Daily sync", "desc", sampleConfig(), null, 1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobUuid").value(jobUuid.toString()))
                .andExpect(jsonPath("$.currentVersion").value(1));

        verify(jobFacade).createJob(any());
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {
        when(permissionCheckerPort.hasPermission(11L, "job:create")).thenReturn(true);

        mockMvc.perform(post("/api/v1/jobs")
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateJobRequest("", null, sampleConfig(), null, 1))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jobFacade);
    }

    @Test
    void shouldReturnUnauthorizedWhenRequestIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/jobs")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenCreatePermissionIsMissing() throws Exception {
        when(permissionCheckerPort.hasPermission(11L, "job:create")).thenReturn(false);

        mockMvc.perform(post("/api/v1/jobs")
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateJobRequest("Daily sync", null, sampleConfig(), null, 1))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnJobWhenUuidPathIsBound() throws Exception {
        UUID jobUuid = UUID.randomUUID();
        when(jobFacade.getJob(jobUuid)).thenReturn(jobResponse(jobUuid, 2));

        mockMvc.perform(get("/api/v1/jobs/{jobUuid}", jobUuid).with(authentication(auth(11L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVersion").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenJobMissing() throws Exception {
        UUID jobUuid = UUID.randomUUID();
        when(jobFacade.getJob(jobUuid)).thenThrow(new JobNotFoundException("Job not found"));

        mockMvc.perform(get("/api/v1/jobs/{jobUuid}", jobUuid).with(authentication(auth(11L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListUpdateDeleteChangeStatusAndTriggerJobs() throws Exception {
        UUID jobUuid = UUID.randomUUID();
        when(jobFacade.listJobs(null, 20, 0)).thenReturn(List.of(jobResponse(jobUuid, 1)));
        when(permissionCheckerPort.hasPermission(11L, "job:update")).thenReturn(true);
        when(permissionCheckerPort.hasPermission(11L, "job:delete")).thenReturn(true);
        when(permissionCheckerPort.hasPermission(11L, "job:execute")).thenReturn(true);
        when(jobFacade.updateJob(any(), any())).thenReturn(jobResponse(jobUuid, 2));
        when(jobFacade.changeStatus(any(), any())).thenReturn(jobResponse(jobUuid, 2));
        when(jobFacade.triggerJob(any(), any())).thenReturn(new TriggerResponse(UUID.randomUUID(), "Execution triggered"));

        mockMvc.perform(get("/api/v1/jobs").with(authentication(auth(11L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobUuid").value(jobUuid.toString()));

        mockMvc.perform(put("/api/v1/jobs/{jobUuid}", jobUuid)
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateJobRequest("Daily sync v2", "new", sampleConfig(), "queue", 7))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVersion").value(2));

        mockMvc.perform(patch("/api/v1/jobs/{jobUuid}/status", jobUuid)
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeJobStatusRequest(JobStatus.DISABLED))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/jobs/{jobUuid}/trigger", jobUuid)
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TriggerJobRequest(null, UUID.randomUUID(), Map.of("k", "v")))))
                .andExpect(status().isAccepted());

        mockMvc.perform(delete("/api/v1/jobs/{jobUuid}", jobUuid).with(authentication(auth(11L))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnValidationErrorForInvalidStatusTransitionPayload() throws Exception {
        when(permissionCheckerPort.hasPermission(11L, "job:update")).thenReturn(true);

        mockMvc.perform(patch("/api/v1/jobs/{jobUuid}/status", UUID.randomUUID())
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private static JobResponse jobResponse(UUID jobUuid, int currentVersion) {
        return new JobResponse(1L, jobUuid, "Daily sync", "desc", "ACTIVE", "default", 1, 11L,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"),
                new JobConfigResponse(
                        new JobTargetResponse("HTTP", "https://example.com", "POST", Map.of(), ""),
                        new RetryPolicyResponse(3, 1000, 2.0, List.of("5xx")),
                        1000,
                        List.of()
                ), currentVersion, 1, false);
    }

    private static JobConfig sampleConfig() {
        return new JobConfig(
                JobTarget.forHttp("https://example.com", "POST", Map.of(), null),
                RetryPolicy.defaultPolicy(),
                1_000,
                List.of()
        );
    }

    private static Authentication auth(Long userId) {
        return UsernamePasswordAuthenticationToken.authenticated(userId, "n/a", List.of());
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

    @RestControllerAdvice
    static class TestExceptionAdvice {
        @ExceptionHandler(JobNotFoundException.class)
        public org.springframework.http.ResponseEntity<Void> handleNotFound() {
            return org.springframework.http.ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        @ExceptionHandler(InvalidJobStateException.class)
        public org.springframework.http.ResponseEntity<Void> handleInvalidState() {
            return org.springframework.http.ResponseEntity.badRequest().build();
        }
    }
}

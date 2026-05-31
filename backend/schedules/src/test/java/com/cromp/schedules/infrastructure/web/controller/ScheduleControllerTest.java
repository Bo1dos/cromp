package com.cromp.schedules.infrastructure.web.controller;

import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.schedules.api.dto.request.CreateUpdateScheduleRequest;
import com.cromp.schedules.api.dto.response.ScheduleResponse;
import com.cromp.schedules.api.service.ScheduleFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({ScheduleController.class, ScheduleControllerTest.TestSecurityConfig.class})
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @org.springframework.boot.test.mock.mockito.MockBean
    private ScheduleFacade scheduleFacade;
    @org.springframework.boot.test.mock.mockito.MockBean(name = "permissionCheckerPort")
    private PermissionCheckerPort permissionCheckerPort;

    @Test
    void putShouldReturnOkAndSerializeResponseWhenPermissionExists() throws Exception {
        UUID jobUuid = UUID.randomUUID();
        ScheduleResponse response = scheduleResponse();
        when(permissionCheckerPort.hasPermission(11L, "job:manage")).thenReturn(true);
        when(scheduleFacade.createOrUpdate(eq(jobUuid), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/jobs/{jobUuid}/schedule", jobUuid)
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUpdateScheduleRequest("*/5 * * * *", "Europe/Moscow", "{\"enabled\":true}"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cronExpression").value("*/5 * * * *"))
                .andExpect(jsonPath("$.timezone").value("Europe/Moscow"))
                .andExpect(jsonPath("$.rules").value("{\"enabled\":true}"))
                .andExpect(jsonPath("$.nextRunAt").value("2024-01-01T02:00:00Z"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(scheduleFacade).createOrUpdate(eq(jobUuid), any());
    }

    @Test
    void putShouldReturnBadRequestForValidationErrors() throws Exception {
        when(permissionCheckerPort.hasPermission(11L, "job:manage")).thenReturn(true);

        mockMvc.perform(put("/api/v1/jobs/{jobUuid}/schedule", UUID.randomUUID())
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUpdateScheduleRequest("", "", null))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(scheduleFacade);
    }

    @Test
    void getShouldReturnOkAndSerializeResponseWhenAuthenticated() throws Exception {
        UUID jobUuid = UUID.randomUUID();
        when(scheduleFacade.getSchedule(jobUuid)).thenReturn(scheduleResponse());

        mockMvc.perform(get("/api/v1/jobs/{jobUuid}/schedule", jobUuid)
                        .with(authentication(auth(11L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").value("2024-01-01T00:00:00Z"));

        verify(scheduleFacade).getSchedule(jobUuid);
    }

    @Test
    void deleteShouldReturnNoContentWhenPermissionExists() throws Exception {
        UUID jobUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "job:manage")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/jobs/{jobUuid}/schedule", jobUuid)
                        .with(authentication(auth(11L))))
                .andExpect(status().isNoContent());

        verify(scheduleFacade).deleteSchedule(jobUuid);
    }

    @Test
    void putShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(put("/api/v1/jobs/{jobUuid}/schedule", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUpdateScheduleRequest("*/5 * * * *", "UTC", null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putShouldReturnForbiddenWithoutPermission() throws Exception {
        when(permissionCheckerPort.hasPermission(11L, "job:manage")).thenReturn(false);

        mockMvc.perform(put("/api/v1/jobs/{jobUuid}/schedule", UUID.randomUUID())
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUpdateScheduleRequest("*/5 * * * *", "UTC", null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/{jobUuid}/schedule", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteShouldReturnForbiddenWithoutPermission() throws Exception {
        when(permissionCheckerPort.hasPermission(11L, "job:manage")).thenReturn(false);

        mockMvc.perform(delete("/api/v1/jobs/{jobUuid}/schedule", UUID.randomUUID())
                        .with(authentication(auth(11L))))
                .andExpect(status().isForbidden());
    }
    @Test
    void deleteShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(delete("/api/v1/jobs/{jobUuid}/schedule", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void putShouldRejectMaxLengthViolations() throws Exception {
        when(permissionCheckerPort.hasPermission(11L, "job:manage")).thenReturn(true);

        mockMvc.perform(put("/api/v1/jobs/{jobUuid}/schedule", UUID.randomUUID())
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUpdateScheduleRequest(
                                "a".repeat(129),
                                "a".repeat(65),
                                null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putShouldAcceptExactBoundaryLengths() throws Exception {
        UUID jobUuid = UUID.randomUUID();
        when(permissionCheckerPort.hasPermission(11L, "job:manage")).thenReturn(true);
        when(scheduleFacade.createOrUpdate(eq(jobUuid), any())).thenReturn(scheduleResponse());

        mockMvc.perform(put("/api/v1/jobs/{jobUuid}/schedule", jobUuid)
                        .with(authentication(auth(11L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUpdateScheduleRequest(
                                "a".repeat(128),
                                "b".repeat(64),
                                "{\"valid\":true}"))))
                .andExpect(status().isOk());
    }

    private static ScheduleResponse scheduleResponse() {
        return new ScheduleResponse("*/5 * * * *", "Europe/Moscow", "{\"enabled\":true}",
                Instant.parse("2024-01-01T02:00:00Z"), "ACTIVE",
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"));
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
}

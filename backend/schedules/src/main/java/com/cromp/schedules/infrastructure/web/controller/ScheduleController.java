package com.cromp.schedules.infrastructure.web.controller;

import com.cromp.schedules.api.dto.request.CreateUpdateScheduleRequest;
import com.cromp.schedules.api.dto.response.ScheduleResponse;
import com.cromp.schedules.api.service.ScheduleFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs/{jobUuid}/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleFacade scheduleFacade;

    @PutMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:manage')")
    public ScheduleResponse createOrUpdate(@PathVariable UUID jobUuid,
                                           @Valid @RequestBody CreateUpdateScheduleRequest request) {
        return scheduleFacade.createOrUpdate(jobUuid, request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ScheduleResponse get(@PathVariable UUID jobUuid) {
        return scheduleFacade.getSchedule(jobUuid);
    }

    @DeleteMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID jobUuid) {
        scheduleFacade.deleteSchedule(jobUuid);
    }
}
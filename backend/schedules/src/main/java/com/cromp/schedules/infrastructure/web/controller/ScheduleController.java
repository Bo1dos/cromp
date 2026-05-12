package com.cromp.schedules.infrastructure.web.controller;

import com.cromp.schedules.api.dto.request.CreateUpdateScheduleRequest;
import com.cromp.schedules.api.dto.response.ScheduleResponse;
import com.cromp.schedules.api.service.ScheduleFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/jobs/{jobId}/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleFacade scheduleFacade;

    @PutMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:manage')")
    public ScheduleResponse createOrUpdate(@PathVariable Long organizationId,
                                           @PathVariable Long jobId,
                                           @Valid @RequestBody CreateUpdateScheduleRequest request) {
        return scheduleFacade.createOrUpdate(organizationId, jobId, request);
    }

    @GetMapping
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public ScheduleResponse get(@PathVariable Long organizationId,
                                @PathVariable Long jobId) {
        return scheduleFacade.getSchedule(organizationId, jobId);
    }

    @DeleteMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long organizationId,
                       @PathVariable Long jobId) {
        scheduleFacade.deleteSchedule(organizationId, jobId);
    }
}
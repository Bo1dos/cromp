package com.cromp.executions.infrastructure.web.controller;

import com.cromp.executions.api.dto.request.CreateExecutionArtifactRequest;
import com.cromp.executions.api.dto.request.CreateExecutionRequest;
import com.cromp.executions.api.dto.request.UpdateAttemptStatusRequest;
import com.cromp.executions.api.dto.response.ExecutionArtifactResponse;
import com.cromp.executions.api.dto.response.ExecutionAttemptResponse;
import com.cromp.executions.api.dto.response.ExecutionResponse;
import com.cromp.executions.api.service.ExecutionFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/executions")
@RequiredArgsConstructor
public class ExecutionController {
    private final ExecutionFacade executionFacade;

    @PostMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:execute')")
    @ResponseStatus(HttpStatus.CREATED)
    public ExecutionResponse create(@PathVariable Long organizationId, @Valid @RequestBody CreateExecutionRequest request) {
        return executionFacade.createExecution(organizationId, request);
    }

    @GetMapping
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<ExecutionResponse> list(@PathVariable Long organizationId,
                                        @RequestParam(required = false) Long jobId) {
        return executionFacade.listExecutions(organizationId, jobId);
    }

    @GetMapping("/{executionId}")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public ExecutionResponse get(@PathVariable Long organizationId, @PathVariable Long executionId) {
        return executionFacade.getExecution(organizationId, executionId);
    }

    @PostMapping("/{executionId}/cancel")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:execute')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long organizationId, @PathVariable Long executionId) {
        executionFacade.cancelExecution(organizationId, executionId);
    }

    @GetMapping("/{executionId}/attempts")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<ExecutionAttemptResponse> attempts(@PathVariable Long organizationId,
                                                   @PathVariable Long executionId) {
        return executionFacade.listAttempts(organizationId, executionId);
    }

    @PatchMapping("/attempts/{attemptId}/status")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:execute')")
    public ExecutionAttemptResponse updateAttemptStatus(@PathVariable Long organizationId,
                                                        @PathVariable Long attemptId,
                                                        @Valid @RequestBody UpdateAttemptStatusRequest request) {
        return executionFacade.updateAttemptStatus(organizationId, attemptId, request);
    }

    @PostMapping("/{executionId}/artifacts")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:execute')")
    @ResponseStatus(HttpStatus.CREATED)
    public ExecutionArtifactResponse addArtifact(@PathVariable Long organizationId,
                                                 @PathVariable Long executionId,
                                                 @Valid @RequestBody CreateExecutionArtifactRequest request) {
        return executionFacade.addArtifact(organizationId, executionId, request);
    }

    @GetMapping("/{executionId}/artifacts")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<ExecutionArtifactResponse> artifacts(@PathVariable Long organizationId,
                                                     @PathVariable Long executionId) {
        return executionFacade.listArtifacts(organizationId, executionId);
    }
}

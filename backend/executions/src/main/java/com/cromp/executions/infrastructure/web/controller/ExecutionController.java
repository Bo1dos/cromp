package com.cromp.executions.infrastructure.web.controller;

import com.cromp.executions.api.dto.ExecutionFilter;
import com.cromp.executions.api.dto.request.UploadArtifactRequest;
import com.cromp.executions.api.dto.response.*;
import com.cromp.executions.application.service.ArtifactService;
import com.cromp.executions.application.service.AttemptService;
import com.cromp.executions.application.service.ExecutionService;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/executions")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;
    private final AttemptService attemptService;
    private final ArtifactService artifactService;

    // GET /organizations/{organizationId}/executions
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<ExecutionResponse> list(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam(name = "jobId", required = false) Long jobId,
            @RequestParam(name = "status", required = false) ExecutionStatus status,
            @RequestParam(name = "source", required = false) ExecutionSource source,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "page", defaultValue = "0")  int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        ExecutionFilter filter = new ExecutionFilter(jobId, status, source, from, to, page, size);
        return executionService.listExecutions(organizationId, filter);
    }

    // GET /organizations/{organizationId}/executions/{executionId}
    @GetMapping("/{executionId}")
    @PreAuthorize("isAuthenticated()")
    public ExecutionDetailResponse get(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("executionId") UUID executionId
    ) {
        return executionService.getExecution(organizationId, executionId);
    }

    // POST /organizations/{organizationId}/executions/{executionId}/cancel
    @PostMapping("/{executionId}/cancel")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:execute')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("executionId") UUID executionId
    ) {
        executionService.cancelExecution(organizationId, executionId);
    }

    // GET /organizations/{organizationId}/executions/{executionId}/attempts
    @GetMapping("/{executionId}/attempts")
    @PreAuthorize("isAuthenticated()")
    public List<AttemptResponse> listAttempts(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("executionId") UUID executionId
    ) {
        return attemptService.listAttempts(organizationId, executionId);
    }

    // GET /organizations/{organizationId}/executions/{executionId}/artifacts
    @GetMapping("/{executionId}/artifacts")
    @PreAuthorize("isAuthenticated()")
    public List<ArtifactResponse> listArtifacts(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("executionId") UUID executionId
    ) {
        return artifactService.listArtifacts(organizationId, executionId);
    }

    // POST /organizations/{organizationId}/executions/{executionId}/artifacts
    @PostMapping(value = "/{executionId}/artifacts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:execute')")
    @ResponseStatus(HttpStatus.CREATED)
    public ArtifactResponse uploadArtifact(
            @PathVariable("organizationId") Long organizationId,
            @PathVariable("executionId") UUID executionId,
            @RequestPart("meta") UploadArtifactRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        return artifactService.uploadArtifact(organizationId, executionId, request, file);
    }
}

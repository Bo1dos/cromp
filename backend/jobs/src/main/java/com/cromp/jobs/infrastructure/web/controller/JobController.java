package com.cromp.jobs.infrastructure.web.controller;

import com.cromp.jobs.api.dto.request.*;
import com.cromp.jobs.api.dto.response.*;
import com.cromp.jobs.api.service.JobFacade;
import com.cromp.jobs.api.service.JobVersionFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobFacade jobFacade;
    private final JobVersionFacade jobVersionFacade;

    // ---- CRUD ----

    @PostMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse create(@PathVariable Long organizationId,
                              @Valid @RequestBody CreateJobRequest request) {
        return jobFacade.createJob(organizationId, request);
    }

    @GetMapping
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<JobResponse> list(@PathVariable Long organizationId,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(defaultValue = "20") int limit,
                                  @RequestParam(defaultValue = "0") int offset) {
        return jobFacade.listJobs(organizationId, status, limit, offset);
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public JobResponse get(@PathVariable Long organizationId,
                           @PathVariable Long jobId) {
        return jobFacade.getJob(organizationId, jobId);
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:update')")
    public JobResponse update(@PathVariable Long organizationId,
                              @PathVariable Long jobId,
                              @Valid @RequestBody UpdateJobRequest request) {
        return jobFacade.updateJob(organizationId, jobId, request);
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long organizationId,
                       @PathVariable Long jobId) {
        jobFacade.deleteJob(organizationId, jobId);
    }

    @PatchMapping("/{jobId}/status")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:update')")
    public JobResponse changeStatus(@PathVariable Long organizationId,
                                    @PathVariable Long jobId,
                                    @Valid @RequestBody ChangeJobStatusRequest request) {
        return jobFacade.changeStatus(organizationId, jobId, request);
    }

    // ---- Manual Trigger ----

    @PostMapping("/{jobId}/trigger")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:execute')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TriggerResponse trigger(@PathVariable Long organizationId,
                                   @PathVariable Long jobId,
                                   @Valid @RequestBody TriggerJobRequest request) {
        return jobFacade.triggerJob(organizationId, jobId, request);
    }

    // ---- Versioning ----

    @GetMapping("/{jobId}/versions")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<JobVersionResponse> getVersions(@PathVariable Long organizationId,
                                                @PathVariable Long jobId) {
        return jobVersionFacade.getVersions(organizationId, jobId);
    }

    @GetMapping("/{jobId}/versions/{version}")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public JobVersionResponse getVersion(@PathVariable Long organizationId,
                                         @PathVariable Long jobId,
                                         @PathVariable int version) {
        return jobVersionFacade.getVersion(organizationId, jobId, version);
    }

    @GetMapping("/{jobId}/versions/compare")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public JobVersionComparisonResponse compareVersions(@PathVariable Long organizationId,
                                                        @PathVariable Long jobId,
                                                        @RequestParam int fromVersion,
                                                        @RequestParam int toVersion) {
        return jobVersionFacade.compareVersions(organizationId, jobId, fromVersion, toVersion);
    }

    @PostMapping("/{jobId}/revert")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'job:update')")
    public JobVersionResponse revert(@PathVariable Long organizationId,
                                     @PathVariable Long jobId,
                                     @Valid @RequestBody RevertJobRequest request) {
        return jobVersionFacade.revertToVersion(organizationId, jobId, request.version());
    }

    @GetMapping("/{jobId}/current-version")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public JobVersionResponse currentVersion(@PathVariable Long organizationId,
                                             @PathVariable Long jobId) {
        return jobVersionFacade.getCurrentVersion(organizationId, jobId);
    }
}
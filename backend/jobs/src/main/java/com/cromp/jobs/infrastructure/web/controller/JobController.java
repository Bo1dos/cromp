package com.cromp.jobs.infrastructure.web.controller;

import com.cromp.jobs.api.dto.request.*;
import com.cromp.jobs.api.dto.response.*;
import com.cromp.jobs.api.service.JobFacade;
import com.cromp.jobs.api.service.JobVersionFacade;
import com.cromp.jobs.domain.model.enums.JobStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobFacade jobFacade;
    private final JobVersionFacade jobVersionFacade;

    // ---- CRUD ----

    @PostMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse create(@Valid @RequestBody CreateJobRequest request) {
        return jobFacade.createJob(request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<JobResponse> list(@RequestParam(name = "status", required = false) JobStatus status,
                                  @RequestParam(name = "limit", defaultValue = "20") int limit,
                                  @RequestParam(name = "offset", defaultValue = "0") int offset) {
        return jobFacade.listJobs(status, limit, offset);
    }

    @GetMapping("/{jobUuid}")
    @PreAuthorize("isAuthenticated()")
    public JobResponse get(@PathVariable("jobUuid") UUID jobUuid) {
        return jobFacade.getJob(jobUuid);
    }

    @PutMapping("/{jobUuid}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:update')")
    public JobResponse update(@PathVariable("jobUuid") UUID jobUuid,
                              @Valid @RequestBody UpdateJobRequest request) {
        return jobFacade.updateJob(jobUuid, request);
    }

    @DeleteMapping("/{jobUuid}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("jobUuid") UUID jobUuid) {
        jobFacade.deleteJob(jobUuid);
    }

    @PatchMapping("/{jobUuid}/status")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:update')")
    public JobResponse changeStatus(@PathVariable("jobUuid") UUID jobUuid,
                                    @Valid @RequestBody ChangeJobStatusRequest request) {
        return jobFacade.changeStatus(jobUuid, request);
    }

    // ---- Manual Trigger ----

    @PostMapping("/{jobUuid}/trigger")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:execute')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TriggerResponse trigger(@PathVariable("jobUuid") UUID jobUuid,
                                   @Valid @RequestBody TriggerJobRequest request) {
        return jobFacade.triggerJob(jobUuid, request);
    }

    // ---- Versioning ----

    @GetMapping("/{jobUuid}/versions")
    @PreAuthorize("isAuthenticated()")
    public List<JobVersionResponse> getVersions(@PathVariable("jobUuid") UUID jobUuid) {
        return jobVersionFacade.getVersions(jobUuid);
    }

    @GetMapping("/{jobUuid}/versions/{version}")
    @PreAuthorize("isAuthenticated()")
    public JobVersionResponse getVersion(@PathVariable("jobUuid") UUID jobUuid,
                                         @PathVariable("version") int version) {
        return jobVersionFacade.getVersion(jobUuid, version);
    }

    @GetMapping("/{jobUuid}/versions/compare")
    @PreAuthorize("isAuthenticated()")
    public JobVersionComparisonResponse compareVersions(@PathVariable("jobUuid") UUID jobUuid,
                                                        @RequestParam(name = "fromVersion") int fromVersion,
                                                        @RequestParam(name = "toVersion") int toVersion) {
        return jobVersionFacade.compareVersions(jobUuid, fromVersion, toVersion);
    }

    @PostMapping("/{jobUuid}/revert")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:update')")
    public JobVersionResponse revert(@PathVariable("jobUuid") UUID jobUuid,
                                     @Valid @RequestBody RevertJobRequest request) {
        return jobVersionFacade.revertToVersion(jobUuid, request.version());
    }

    @GetMapping("/{jobUuid}/current-version")
    @PreAuthorize("isAuthenticated()")
    public JobVersionResponse currentVersion(@PathVariable("jobUuid") UUID jobUuid) {
        return jobVersionFacade.getCurrentVersion(jobUuid);
    }
}

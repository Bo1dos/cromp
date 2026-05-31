package com.cromp.jobs.application.service;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.jobs.api.dto.response.*;
import com.cromp.jobs.api.mapper.JobVersionApiMapper;
import com.cromp.jobs.api.service.JobVersionFacade;
import com.cromp.jobs.application.port.AuditPort;
import com.cromp.jobs.domain.model.*;
import com.cromp.jobs.domain.model.exceptions.*;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cromp.jobs.domain.repository.JobVersionRepositoryPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class JobVersionApplicationService implements JobVersionFacade {

    private final JobVersionRepositoryPort versionRepository;
    private final JobRepositoryPort jobRepository;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;
    private final AuditPort auditPort;
    private final JobVersionApiMapper jobVersionApiMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<JobVersionResponse> getVersions(UUID jobUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        Job job = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        return versionRepository.findByJobIdOrderByVersionDesc(job.getId()).stream()
                .map(v -> jobVersionApiMapper.toVersionResponse(job, v))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobVersionResponse getVersion(UUID jobUuid, int version) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        Job job = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        JobVersion ver = versionRepository.findByJobIdAndVersion(job.getId(), version)
                .orElseThrow(() -> new JobVersionNotFoundException(job.getId(), version));
        return jobVersionApiMapper.toVersionResponse(job, ver);
    }

    @Override
    @Transactional(readOnly = true)
    public JobVersionComparisonResponse compareVersions(UUID jobUuid, int fromVersion, int toVersion) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        Job job = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        JobVersion from = versionRepository.findByJobIdAndVersion(job.getId(), fromVersion)
                .orElseThrow(() -> new JobVersionNotFoundException(job.getId(), fromVersion));
        JobVersion to = versionRepository.findByJobIdAndVersion(job.getId(), toVersion)
                .orElseThrow(() -> new JobVersionNotFoundException(job.getId(), toVersion));

        List<JobVersionComparisonResponse.JobVersionDiff> diffs = new ArrayList<>();
        try {
            String fromJson = objectMapper.writeValueAsString(from.getConfig());
            String toJson = objectMapper.writeValueAsString(to.getConfig());
            if (!fromJson.equals(toJson)) {
                diffs.add(new JobVersionComparisonResponse.JobVersionDiff(
                        "config", "version " + fromVersion, "version " + toVersion, "MODIFIED"));
            }
        } catch (JsonProcessingException e) {
            diffs.add(new JobVersionComparisonResponse.JobVersionDiff(
                    "config", "version " + fromVersion, "version " + toVersion, "ERROR"));
        }

        return new JobVersionComparisonResponse(
                fromVersion, toVersion, diffs,
                new JobVersionComparisonResponse.Summary(diffs.size(), 0, List.of())
        );
    }

    @Override
    public JobVersionResponse revertToVersion(UUID jobUuid, int version) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:update")) {
            throw new SecurityException("No permission to revert job");
        }
        Job job = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        Long jobId = job.getId();
        JobVersion source = versionRepository.findByJobIdAndVersion(jobId, version)
                .orElseThrow(() -> new JobVersionNotFoundException(jobId, version));
        JobVersion latest = versionRepository.findLatestByJobId(jobId).orElseThrow();
        int newVersion = latest.getVersion() + 1;
        JobVersion reverted = JobVersion.create(jobId, newVersion, source.getConfig(), userId);
        versionRepository.save(reverted);
        auditPort.record("JOB.REVERT", organizationId, userId, "jobs", jobId,
                Map.of("fromVersion", version, "newVersion", newVersion));
        return jobVersionApiMapper.toVersionResponse(job, reverted);
    }

    @Override
    @Transactional(readOnly = true)
    public JobVersionResponse getCurrentVersion(UUID jobUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        Job job = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        JobVersion latest = versionRepository.findLatestByJobId(job.getId())
                .orElseThrow(() -> new JobVersionNotFoundException(job.getId(), -1));
        return jobVersionApiMapper.toVersionResponse(job, latest);
    }

    private Job checkJobOwnership(Long organizationId, Long jobId) {
        return jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private void ensureMembership(Long orgId) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, orgId)) {
            throw new SecurityException("Not a member of this organization");
        }
    }

    private void ensureOrganizationAccess(Job job, Long organizationId) {
        if (!Objects.equals(job.getOrganizationId(), organizationId)) {
            throw new SecurityException("Foreign organization access denied");
        }
    }
}

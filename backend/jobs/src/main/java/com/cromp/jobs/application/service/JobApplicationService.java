package com.cromp.jobs.application.service;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.jobs.api.dto.request.*;
import com.cromp.jobs.api.dto.response.*;
import com.cromp.jobs.api.mapper.JobApiMapper;
import com.cromp.jobs.api.service.JobFacade;
import com.cromp.jobs.application.port.AuditPort;
import com.cromp.jobs.application.port.ExecutionCreationPort;
import com.cromp.jobs.domain.model.*;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.model.exceptions.*;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cromp.jobs.domain.repository.JobVersionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationService implements JobFacade {

    private final JobRepositoryPort jobRepository;
    private final JobVersionRepositoryPort versionRepository;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;
    private final AuditPort auditPort;
    private final ExecutionCreationPort executionCreationPort;
    private final JobApiMapper jobApiMapper;

    @Override
    public JobResponse createJob(Long organizationId, CreateJobRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:create")) {
            throw new SecurityException("No permission to create job in this organization");
        }
        if (jobRepository.existsByNameAndOrganizationId(request.name(), organizationId)) {
            throw new InvalidJobStateException("Job with name '" + request.name() + "' already exists in this organization");
        }

        Job job = Job.create(UUID.randomUUID(), organizationId, request.name(), request.description(),
                request.queueName() != null ? request.queueName() : "default", request.priority(), userId);
        job = jobRepository.save(job);

        JobVersion version = JobVersion.create(job.getId(), 1, request.config(), userId);
        versionRepository.save(version);

        auditPort.record("JOB.CREATE", organizationId, userId, "jobs", job.getId(), Map.of("name", request.name()));

        return jobApiMapper.toJobResponse(job, version, 1);
    }

    @Override
    public JobResponse updateJob(Long organizationId, Long jobId, UpdateJobRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:update")) {
            throw new SecurityException("No permission to update job in this organization");
        }

        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        Map<String, Object> changes = new HashMap<>();

        if (request.name() != null && !request.name().equals(job.getName())) {
            if (jobRepository.existsByNameAndOrganizationId(request.name(), organizationId)) {
                throw new InvalidJobStateException("Name already in use");
            }
            job.rename(request.name());
            changes.put("name", request.name());
        }
        if (request.description() != null) {
            job.changeDescription(request.description());
            changes.put("description", request.description());
        }
        if (request.queueName() != null) {
            job.changeQueue(request.queueName());
            changes.put("queueName", request.queueName());
        }
        if (request.priority() != null) {
            job.changePriority(request.priority());
            changes.put("priority", request.priority());
        }
        job = jobRepository.save(job);

        JobVersion currentVersion = versionRepository.findLatestByJobId(jobId).orElseThrow();
        int newVersionNumber = currentVersion.getVersion();

        if (request.config() != null) {
            newVersionNumber = currentVersion.getVersion() + 1;
            JobVersion newVersion = JobVersion.create(jobId, newVersionNumber, request.config(), userId);
            versionRepository.save(newVersion);
            currentVersion = newVersion;
            changes.put("config", "updated to version " + newVersionNumber);
        }

        if (!changes.isEmpty()) {
            auditPort.record("JOB.UPDATE", organizationId, userId, "jobs", jobId, changes);
        }

        int versionCount = versionRepository.findByJobIdOrderByVersionDesc(jobId).size();
        return jobApiMapper.toJobResponse(job, currentVersion, versionCount);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJob(Long organizationId, Long jobId) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        JobVersion version = versionRepository.findLatestByJobId(jobId)
                .orElseThrow(() -> new JobVersionNotFoundException(jobId, -1));
        int versionCount = versionRepository.findByJobIdOrderByVersionDesc(jobId).size();
        return jobApiMapper.toJobResponse(job, version, versionCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> listJobs(Long organizationId, JobStatus status, int limit, int offset) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }

        List<Job> jobs;
        if (status != null) {
            jobs = jobRepository.findByOrganizationIdAndStatus(organizationId, status);
        } else {
            jobs = jobRepository.findByOrganizationId(organizationId);
        }

        int toIndex = Math.min(offset + limit, jobs.size());
        if (offset >= jobs.size()) return List.of();

        return jobs.subList(offset, toIndex).stream().map(job -> {
            JobVersion ver = versionRepository.findLatestByJobId(job.getId()).orElse(null);
            int vCount = versionRepository.findByJobIdOrderByVersionDesc(job.getId()).size();
            return jobApiMapper.toJobResponse(job, ver, vCount);
        }).toList();
    }

    @Override
    public JobResponse changeStatus(Long organizationId, Long jobId, ChangeJobStatusRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:update")) {
            throw new SecurityException("No permission to change job status");
        }

        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        switch (request.status()) {
            case ACTIVE -> job.activate();
            case DISABLED -> job.disable();
            default -> throw new InvalidJobStateException("Can only change to ACTIVE or DISABLED");
        }

        jobRepository.save(job);
        auditPort.record("JOB.STATUS_CHANGE", organizationId, userId, "jobs", jobId,
                Map.of("status", request.status().name()));

        JobVersion version = versionRepository.findLatestByJobId(jobId).orElseThrow();
        int vCount = versionRepository.findByJobIdOrderByVersionDesc(jobId).size();
        return jobApiMapper.toJobResponse(job, version, vCount);
    }

    @Override
    public TriggerResponse triggerJob(Long organizationId, Long jobId, TriggerJobRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:execute")) {
            throw new SecurityException("No permission to execute job");
        }

        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new InvalidJobStateException("Job must be ACTIVE to trigger manually");
        }

        JobVersion currentVersion = versionRepository.findLatestByJobId(jobId)
                .orElseThrow(() -> new JobVersionNotFoundException(jobId, -1));

        Map<String, Object> payload = request.parameters() != null ? request.parameters() : Map.of();
        UUID correlationId = request.correlationId() != null ? request.correlationId() : UUID.randomUUID();

        UUID executionId = executionCreationPort.createExecution(
                organizationId, jobId, currentVersion.getId(), "MANUAL", userId,
                payload, correlationId
        );

        auditPort.record("JOB.TRIGGER", organizationId, userId, "jobs", jobId,
                Map.of("executionId", executionId.toString()));

        return new TriggerResponse(executionId, "Execution triggered");
    }

    @Override
    public void deleteJob(Long organizationId, Long jobId) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:delete")) {
            throw new SecurityException("No permission to delete job");
        }
        Job job = jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        job.archive();
        jobRepository.save(job);
        auditPort.record("JOB.ARCHIVE", organizationId, userId, "jobs", jobId, Map.of("status", "ARCHIVED"));
    }

}
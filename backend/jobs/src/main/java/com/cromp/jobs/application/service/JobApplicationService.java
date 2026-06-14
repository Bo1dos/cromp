package com.cromp.jobs.application.service;

import com.cromp.common.event.integration.publisher.DomainEventPublisher;
import com.cromp.common.event.domain.job.JobDisabledEvent;
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
import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.repository.SecretRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationService implements JobFacade {

    private final JobRepositoryPort jobRepository;
    private final JobVersionRepositoryPort versionRepository;
    private final SecretRepositoryPort secretRepository;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;
    private final AuditPort auditPort;
    private final ExecutionCreationPort executionCreationPort;
    private final JobApiMapper jobApiMapper;
    private final JdbcTemplate jdbcTemplate;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public JobResponse createJob(CreateJobRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:create")) {
            throw new SecurityException("No permission to create job in this organization");
        }
        if (jobRepository.existsByNameAndOrganizationId(request.name(), organizationId)) {
            throw new InvalidJobStateException("Job with name '" + request.name() + "' already exists in this organization");
        }

        Job job = Job.create(UUID.randomUUID(), organizationId, request.name(), request.description(),
                request.queueName() != null ? request.queueName() : "default", request.priority(), userId);
        job = jobRepository.save(job);

        validateSecretRefs(request.config(), organizationId);

        JobVersion version = JobVersion.create(job.getId(), 1, request.config(), userId);
        versionRepository.save(version);

        auditPort.record("JOB.CREATE", organizationId, userId, "jobs", job.getId(), Map.of("name", request.name()));

        return jobApiMapper.toJobResponse(job, version, 1);
    }

    @Override
    public JobResponse updateJob(UUID jobUuid, UpdateJobRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:update")) {
            throw new SecurityException("No permission to update job in this organization");
        }

        Job job = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        if (job.isDeleted()) {
            throw new JobNotFoundException("Job not found");
        }
        Long jobId = job.getId();

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
            validateSecretRefs(request.config(), organizationId);
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
    public JobResponse getJob(UUID jobUuid) {
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
        if (job.isDeleted()) {
            throw new JobNotFoundException("Job not found");
        }
        JobVersion version = versionRepository.findLatestByJobId(job.getId())
                .orElseThrow(() -> new JobVersionNotFoundException(job.getId(), -1));
        int versionCount = versionRepository.findByJobIdOrderByVersionDesc(job.getId()).size();
        return jobApiMapper.toJobResponse(job, version, versionCount);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        if (job.isDeleted()) {
            throw new JobNotFoundException("Job not found");
        }
        JobVersion version = versionRepository.findLatestByJobId(job.getId())
                .orElseThrow(() -> new JobVersionNotFoundException(job.getId(), -1));
        int versionCount = versionRepository.findByJobIdOrderByVersionDesc(job.getId()).size();
        return jobApiMapper.toJobResponse(job, version, versionCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> listJobs(JobStatus status, int limit, int offset) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
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

        // Query all job IDs in this org that have schedules
        Set<Long> scheduledJobIds = new HashSet<>(jdbcTemplate.queryForList(
                "SELECT s.job_id FROM schedules s JOIN jobs j ON j.id = s.job_id WHERE j.organization_id = ?",
                Long.class, organizationId));

        return jobs.subList(offset, toIndex).stream().map(job -> {
            JobVersion ver = versionRepository.findLatestByJobId(job.getId()).orElse(null);
            int vCount = versionRepository.findByJobIdOrderByVersionDesc(job.getId()).size();
            boolean hasSched = scheduledJobIds.contains(job.getId());
            return jobApiMapper.toJobResponse(job, ver, vCount, hasSched);
        }).toList();
    }

    @Override
    public JobResponse changeStatus(UUID jobUuid, ChangeJobStatusRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:update")) {
            throw new SecurityException("No permission to change job status");
        }

        Job job = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        if (job.isDeleted()) {
            throw new JobNotFoundException("Job not found");
        }
        Long jobId = job.getId();

        switch (request.status()) {
            case ACTIVE -> {
                if (job.getStatus() == JobStatus.ACTIVE) {
                    throw new InvalidJobStateException("Job is already ACTIVE");
                }
                job.activate();
            }
            case DISABLED -> {
                if (job.getStatus() == JobStatus.DISABLED) {
                    throw new InvalidJobStateException("Job is already DISABLED");
                }
                job.disable();
            }
            default -> throw new InvalidJobStateException("Can only change to ACTIVE or DISABLED");
        }

        jobRepository.save(job);
        auditPort.record("JOB.STATUS_CHANGE", organizationId, userId, "jobs", jobId,
                Map.of("status", request.status().name()));

        if (request.status() == JobStatus.DISABLED) {
            domainEventPublisher.publish(new JobDisabledEvent(
                    job.getJobUuid(), job.getName(), organizationId, userId));
        }

        JobVersion version = versionRepository.findLatestByJobId(jobId).orElseThrow();
        int vCount = versionRepository.findByJobIdOrderByVersionDesc(jobId).size();
        return jobApiMapper.toJobResponse(job, version, vCount);
    }

    @Override
    public TriggerResponse triggerJob(UUID jobUuid, TriggerJobRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:execute")) {
            throw new SecurityException("No permission to execute job");
        }

        Job job = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        if (job.isDeleted()) {
            throw new JobNotFoundException("Job not found");
        }
        Long jobId = job.getId();
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new InvalidJobStateException("Job must be ACTIVE to trigger manually");
        }

        JobVersion currentVersion = versionRepository.findLatestByJobId(jobId)
                .orElseThrow(() -> new JobVersionNotFoundException(jobId, -1));

        Map<String, Object> payload = request.parameters() != null ? request.parameters() : Map.of();
        UUID correlationId = request.correlationId() != null ? request.correlationId() : UUID.randomUUID();

        UUID executionId = executionCreationPort.createExecution(
                organizationId, jobId, currentVersion.getId(),
                "MANUAL", userId,
                payload, correlationId,
                job.getPriority(),   // priority берём с job
                null,                // scheduledAt — null для ручного запуска
                null                 // executionPolicySnapshot — возьмётся из jobVersion в ExecutionService
        );

        auditPort.record("JOB.TRIGGER", organizationId, userId, "jobs", jobId,
                Map.of("executionId", executionId.toString()));

        return new TriggerResponse(executionId, "Execution triggered");
    }

    @Override
    public void deleteJob(UUID jobUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:delete")) {
            throw new SecurityException("No permission to delete job");
        }
        Job job = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        ensureOrganizationAccess(job, organizationId);
        if (job.isDeleted()) {
            throw new InvalidJobStateException("Job is already deleted");
        }
        Long jobId = job.getId();
        job.archive();
        jobRepository.save(job);
        auditPort.record("JOB.ARCHIVE", organizationId, userId, "jobs", jobId, Map.of("status", "ARCHIVED"));
    }

    private void ensureOrganizationAccess(Job job, Long organizationId) {
        if (!Objects.equals(job.getOrganizationId(), organizationId)) {
            throw new SecurityException("Foreign organization access denied");
        }
    }

    /**
     * Проверяет, что все секреты из JobConfig существуют, не удалены
     * и принадлежат той же организации.
     */
    private void validateSecretRefs(JobConfig config, Long organizationId) {
        if (config == null || config.secrets() == null || config.secrets().isEmpty()) {
            return;
        }
        for (var ref : config.secrets()) {
            Secret secret = secretRepository.findBySecretUuid(ref.secretId())
                    .orElse(null);
            if (secret == null || secret.isDeleted()
                    || !Objects.equals(secret.getOrganizationId(), organizationId)) {
                throw new InvalidJobStateException(
                        "Secret not found or not accessible: " + ref.secretId());
            }
        }
    }

}

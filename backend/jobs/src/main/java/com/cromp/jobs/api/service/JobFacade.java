package com.cromp.jobs.api.service;

import com.cromp.jobs.api.dto.request.*;
import com.cromp.jobs.api.dto.response.*;
import com.cromp.jobs.domain.model.enums.JobStatus;

import java.util.List;
import java.util.UUID;

public interface JobFacade {
    JobResponse createJob(CreateJobRequest request);
    JobResponse updateJob(UUID jobUuid, UpdateJobRequest request);
    JobResponse getJob(UUID jobUuid);
    List<JobResponse> listJobs(JobStatus status, int limit, int offset);
    void deleteJob(UUID jobUuid);
    JobResponse changeStatus(UUID jobUuid, ChangeJobStatusRequest request);
    TriggerResponse triggerJob(UUID jobUuid, TriggerJobRequest request);
}
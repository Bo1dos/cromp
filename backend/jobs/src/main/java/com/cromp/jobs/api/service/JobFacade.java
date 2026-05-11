package com.cromp.jobs.api.service;

import com.cromp.jobs.api.dto.request.*;
import com.cromp.jobs.api.dto.response.*;

import java.util.List;

public interface JobFacade {
    JobResponse createJob(Long organizationId, CreateJobRequest request);
    JobResponse updateJob(Long organizationId, Long jobId, UpdateJobRequest request);
    JobResponse getJob(Long organizationId, Long jobId);
    List<JobResponse> listJobs(Long organizationId, String status, int limit, int offset);
    void deleteJob(Long organizationId, Long jobId);
    JobResponse changeStatus(Long organizationId, Long jobId, ChangeJobStatusRequest request);
    TriggerResponse triggerJob(Long organizationId, Long jobId, TriggerJobRequest request);
}
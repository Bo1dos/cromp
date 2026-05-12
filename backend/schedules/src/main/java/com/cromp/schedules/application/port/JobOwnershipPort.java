package com.cromp.schedules.application.port;

public interface JobOwnershipPort {
    boolean jobBelongsToOrganization(Long jobId, Long organizationId);
}
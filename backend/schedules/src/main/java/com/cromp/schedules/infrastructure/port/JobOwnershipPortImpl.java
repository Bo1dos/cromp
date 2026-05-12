package com.cromp.schedules.infrastructure.port;

import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cromp.schedules.application.port.JobOwnershipPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobOwnershipPortImpl implements JobOwnershipPort {

    private final JobRepositoryPort jobRepository;

    @Override
    public boolean jobBelongsToOrganization(Long jobId, Long organizationId) {
        return jobRepository.findByIdAndOrganizationId(jobId, organizationId).isPresent();
    }
}
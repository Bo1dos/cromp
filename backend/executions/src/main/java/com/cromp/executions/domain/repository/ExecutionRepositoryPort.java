package com.cromp.executions.domain.repository;

import com.cromp.executions.domain.model.Execution;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionRepositoryPort {
    Execution save(Execution execution);
    Optional<Execution> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<Execution> findByExecUuidAndOrganizationId(UUID execUuid, Long organizationId);
    List<Execution> findByOrganizationId(Long organizationId);
    List<Execution> findByJobId(Long organizationId, Long jobId);
}

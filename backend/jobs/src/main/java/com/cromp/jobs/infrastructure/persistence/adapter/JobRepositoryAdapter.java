package com.cromp.jobs.infrastructure.persistence.adapter;

import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cromp.jobs.infrastructure.persistence.jpa.repository.JobJpaRepository;
import com.cromp.jobs.infrastructure.persistence.mapper.JobPersistenceMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class JobRepositoryAdapter implements JobRepositoryPort {

    private final JobJpaRepository repository;
    private final JobPersistenceMapper mapper;

    @Override
    public Job save(Job job) {
        return mapper.toDomain(repository.save(mapper.toJpa(job)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Job> findById(@NonNull Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Job> findByIdAndOrganizationId(Long id, Long organizationId) {
        return repository.findByIdAndOrganizationId(id, organizationId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findByOrganizationIdAndStatus(Long organizationId, JobStatus status) {
        return repository.findByOrganizationIdAndStatus(organizationId, status)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndOrganizationId(String name, Long organizationId) {
        return repository.existsByNameAndOrganizationIdAndDeletedAtIsNull(name, organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Job> findByJobUuid(UUID jobUuid) {
        return repository.findByJobUuid(jobUuid).map(mapper::toDomain);
    }
}
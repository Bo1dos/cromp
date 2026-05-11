package com.cromp.jobs.infrastructure.persistence.adapter;

import com.cromp.jobs.domain.model.JobVersion;
import com.cromp.jobs.domain.repository.JobVersionRepositoryPort;
import com.cromp.jobs.infrastructure.persistence.jpa.repository.JobVersionJpaRepository;
import com.cromp.jobs.infrastructure.persistence.mapper.JobVersionPersistenceMapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class JobVersionRepositoryAdapter implements JobVersionRepositoryPort {

    private final JobVersionJpaRepository repository;
    private final JobVersionPersistenceMapper mapper;

    @Override
    public JobVersion save(@NonNull JobVersion version) {
        return mapper.toDomain(repository.save(mapper.toJpa(version)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobVersion> findById(@NonNull Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobVersion> findByJobIdAndVersion(Long jobId, int version) {
        return repository.findByJobIdAndVersion(jobId, version).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobVersion> findByJobIdOrderByVersionDesc(Long jobId) {
        return repository.findByJobIdOrderByVersionDesc(jobId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobVersion> findLatestByJobId(Long jobId) {
        return repository.findTopByJobIdOrderByVersionDesc(jobId).map(mapper::toDomain);
    }
}
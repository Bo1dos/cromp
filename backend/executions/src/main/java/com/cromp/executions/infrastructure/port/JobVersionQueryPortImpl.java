package com.cromp.executions.infrastructure.port;

import com.cromp.executions.application.port.JobVersionQueryPort;
import com.cromp.jobs.domain.repository.JobVersionRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JobVersionQueryPortImpl implements JobVersionQueryPort {

    private final JobVersionRepositoryPort jobVersionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<String> findConfigJsonByVersionId(Long jobVersionId) {
        return jobVersionRepository.findById(jobVersionId)
                .map(version -> {
                    try {
                        return objectMapper.writeValueAsString(version.getConfig());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize JobConfig", e);
                    }
                });
    }
}
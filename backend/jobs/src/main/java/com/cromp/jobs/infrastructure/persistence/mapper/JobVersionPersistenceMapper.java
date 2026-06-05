package com.cromp.jobs.infrastructure.persistence.mapper;

import com.cromp.jobs.domain.model.JobConfig;
import com.cromp.jobs.domain.model.JobVersion;
import com.cromp.jobs.infrastructure.persistence.jpa.entity.JobVersionJpaEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JobVersionPersistenceMapper {

    private final ObjectMapper objectMapper;

    public JobVersionPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JobVersionJpaEntity toJpa(JobVersion version) {
        JobConfig config = version.getConfig();
        return JobVersionJpaEntity.builder()
                .id(version.getId())
                .jobId(version.getJobId())
                .version(version.getVersion())
                .config(toJson(config))
                .method(config.target().method())
                .targetUrl(config.target().url())
                .timeoutMs(config.timeoutMs())
                .retryPolicy(toJson(config.retryPolicy()))
                .createdBy(version.getCreatedBy())
                .createdAt(version.getCreatedAt())
                .build();
    }

    public JobVersion toDomain(JobVersionJpaEntity entity) {
        return JobVersion.reconstitute(
                entity.getId(),
                entity.getJobId(),
                entity.getVersion(),
                parseConfig(entity.getConfig()),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize " + obj.getClass().getSimpleName(), e);
        }
    }

    private JobConfig parseConfig(String json) {
        try {
            return objectMapper.readValue(json, JobConfig.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JobConfig", e);
        }
    }
}
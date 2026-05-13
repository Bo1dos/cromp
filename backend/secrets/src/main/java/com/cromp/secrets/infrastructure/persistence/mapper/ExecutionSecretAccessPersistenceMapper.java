package com.cromp.secrets.infrastructure.persistence.mapper;

import com.cromp.secrets.domain.model.ExecutionSecretAccess;
import com.cromp.secrets.infrastructure.persistence.jpa.entity.ExecutionSecretAccessJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ExecutionSecretAccessPersistenceMapper {
    public ExecutionSecretAccessJpaEntity toJpa(ExecutionSecretAccess access) {
        return ExecutionSecretAccessJpaEntity.builder()
                .id(access.getId())
                .attemptId(access.getAttemptId())
                .secretVersionId(access.getSecretVersionId())
                .accessedKey(access.getAccessedKey())
                .accessedAt(access.getAccessedAt() != null ? access.getAccessedAt() : Instant.now())
                .build();
    }

    public ExecutionSecretAccess toDomain(ExecutionSecretAccessJpaEntity entity) {
        return ExecutionSecretAccess.builder()
                .id(entity.getId())
                .attemptId(entity.getAttemptId())
                .secretVersionId(entity.getSecretVersionId())
                .accessedKey(entity.getAccessedKey())
                .accessedAt(entity.getAccessedAt())
                .build();
    }
}

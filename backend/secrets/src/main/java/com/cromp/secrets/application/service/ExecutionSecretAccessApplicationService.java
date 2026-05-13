package com.cromp.secrets.application.service;

import com.cromp.secrets.api.dto.request.RecordSecretAccessRequest;
import com.cromp.secrets.domain.model.ExecutionSecretAccess;
import com.cromp.secrets.domain.repository.ExecutionSecretAccessRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ExecutionSecretAccessApplicationService {
    private final ExecutionSecretAccessRepositoryPort repository;

    public void recordAccess(RecordSecretAccessRequest request) {
        repository.save(ExecutionSecretAccess.builder()
                .attemptId(request.attemptId())
                .secretVersionId(request.secretVersionId())
                .accessedKey(request.accessedKey())
                .accessedAt(Instant.now())
                .build());
    }
}

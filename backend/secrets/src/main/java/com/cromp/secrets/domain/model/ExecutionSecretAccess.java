package com.cromp.secrets.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ExecutionSecretAccess {
    private Long id;
    private Long attemptId;
    private Long secretVersionId;
    private String accessedKey;
    private Instant accessedAt;
}

package com.cromp.secrets.domain.repository;

import com.cromp.secrets.domain.model.ExecutionSecretAccess;

public interface ExecutionSecretAccessRepositoryPort {
    ExecutionSecretAccess save(ExecutionSecretAccess access);
}

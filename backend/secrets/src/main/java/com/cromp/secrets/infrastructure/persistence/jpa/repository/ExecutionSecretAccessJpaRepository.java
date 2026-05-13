package com.cromp.secrets.infrastructure.persistence.jpa.repository;

import com.cromp.secrets.infrastructure.persistence.jpa.entity.ExecutionSecretAccessJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionSecretAccessJpaRepository extends JpaRepository<ExecutionSecretAccessJpaEntity, Long> {
}

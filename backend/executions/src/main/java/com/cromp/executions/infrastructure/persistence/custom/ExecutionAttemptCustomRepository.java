package com.cromp.executions.infrastructure.persistence.custom;

import com.cromp.executions.api.dto.response.ClaimAttemptResult;
import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.infrastructure.persistence.jpa.repository.ExecutionAttemptJpaRepository;
import com.cromp.executions.infrastructure.persistence.mapper.ExecutionAttemptPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ExecutionAttemptCustomRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ExecutionAttemptJpaRepository jpaRepository;
    private final ExecutionAttemptPersistenceMapper mapper;

    /**
     * Атомарный захват PENDING попытки через хранимую функцию.
     * SELECT ... FOR UPDATE SKIP LOCKED — внутри функции claim_execution_attempt.
     */
    @Transactional
    public Optional<ClaimAttemptResult> claimAttempt(Long organizationId) {
        List<ClaimAttemptResult> results = jdbcTemplate.query(
                "SELECT attempt_id, execution_id, attempt_uuid, config FROM claim_execution_attempt(?)",
                (rs, rowNum) -> new ClaimAttemptResult(
                        rs.getLong("attempt_id"),
                        rs.getObject("attempt_uuid", UUID.class),
                        rs.getLong("execution_id"),
                        rs.getString("config")
                ),
                organizationId
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Попытки по execUuid + organizationId — для публичного API.
     */
    @Transactional(readOnly = true)
    public List<ExecutionAttempt> findAttemptsByExecUuid(Long organizationId, UUID execUuid) {
        return jpaRepository.findByExecUuidAndOrgId(execUuid, organizationId)
                .stream().map(mapper::toDomain).toList();
    }
}
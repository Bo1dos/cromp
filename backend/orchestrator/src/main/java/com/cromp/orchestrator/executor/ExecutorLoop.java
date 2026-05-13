package com.cromp.orchestrator.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cromp.orchestrator.executor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ExecutorLoop {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final HttpJobExecutor httpJobExecutor;

    @Value("${cromp.orchestrator.executor.organization-id:1}")
    private Long organizationId;

    @Scheduled(fixedDelayString = "${cromp.orchestrator.executor.fixed-delay-ms:1000}")
    @Transactional
    public void tick() {
        Optional<ClaimedAttempt> claimed = claimAttempt(organizationId);
        claimed.ifPresent(this::execute);
    }

    public Optional<ClaimedAttempt> claimAttempt(Long organizationId) {
        List<ClaimedAttempt> attempts = jdbcTemplate.query("SELECT * FROM claim_execution_attempt(?)",
                (rs, rowNum) -> new ClaimedAttempt(
                        rs.getLong("attempt_id"),
                        rs.getLong("execution_id"),
                        rs.getObject("attempt_uuid", UUID.class),
                        readConfig(rs.getObject("config"))
                ),
                organizationId);
        return attempts.stream().findFirst();
    }

    public void execute(ClaimedAttempt attempt) {
        markRunning(attempt.attemptId());
        ExecutionResult result = httpJobExecutor.execute(attempt.config());
        jdbcTemplate.update("""
                UPDATE execution_attempts
                SET status = ?, status_reason = ?, error_class = ?, output_summary = ?::jsonb,
                    finished_at = now(),
                    duration_ms = CASE WHEN started_at IS NULL THEN NULL ELSE EXTRACT(EPOCH FROM (now() - started_at))::int * 1000 END,
                    updated_at = now()
                WHERE id = ?
                """,
                result.succeeded() ? "SUCCEEDED" : "FAILED",
                result.statusReason(),
                result.errorClass(),
                writeJson(result.outputSummary()),
                attempt.attemptId());
        log.info("Execution attempt {} completed with status {}", attempt.attemptUuid(),
                result.succeeded() ? "SUCCEEDED" : "FAILED");
    }

    private void markRunning(Long attemptId) {
        jdbcTemplate.update("""
                UPDATE execution_attempts
                SET status = 'RUNNING', started_at = COALESCE(started_at, ?), updated_at = now()
                WHERE id = ? AND status = 'DISPATCHED'
                """, java.sql.Timestamp.from(Instant.now()), attemptId);
    }

    private Map<String, Object> readConfig(Object value) {
        try {
            String json = value instanceof PGobject pg ? pg.getValue() : String.valueOf(value);
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid execution config JSON", ex);
        }
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid output summary", ex);
        }
    }
}

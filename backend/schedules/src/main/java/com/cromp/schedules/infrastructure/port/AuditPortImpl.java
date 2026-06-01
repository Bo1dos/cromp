package com.cromp.schedules.infrastructure.port;

import com.cromp.iam.domain.model.AuditLogEntry;
import com.cromp.iam.domain.repository.AuditLogRepositoryPort;
import com.cromp.schedules.application.port.AuditPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("schedulesAuditPortImpl")
@RequiredArgsConstructor
public class AuditPortImpl implements AuditPort {

    private final AuditLogRepositoryPort auditLogRepository;

    @Override
    public void record(String action, Long organizationId, Long actorId,
                       String resourceType, Long resourceId, Map<String, Object> changesDiff) {
        AuditLogEntry entry = AuditLogEntry.record(
                organizationId, actorId, Map.of(),
                action, resourceType, resourceId, changesDiff
        );
        auditLogRepository.save(entry);
    }
}
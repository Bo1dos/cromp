package com.cromp.secrets.infrastructure.port;

import com.cromp.iam.api.dto.request.RecordAuditLogRequest;
import com.cromp.iam.api.service.AuditLogFacade;
import com.cromp.secrets.application.port.AuditPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditPortImpl implements AuditPort {

    private final AuditLogFacade auditLogFacade;

    @Override
    public void record(String action, Long organizationId, Long actorId,
                       String resourceType, Long resourceId, Map<String, Object> changesDiff) {
        RecordAuditLogRequest request = new RecordAuditLogRequest(
                organizationId, actorId, null, action, resourceType, resourceId, changesDiff
        );
        auditLogFacade.record(request);
    }
}
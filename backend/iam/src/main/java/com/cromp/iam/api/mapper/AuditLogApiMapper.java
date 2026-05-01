package com.cromp.iam.api.mapper;

import com.cromp.iam.api.dto.response.AuditLogResponse;
import com.cromp.iam.domain.model.AuditLogEntry;
import org.springframework.stereotype.Component;

@Component
public class AuditLogApiMapper {

    public AuditLogResponse toResponse(AuditLogEntry entry) {
        return new AuditLogResponse(
                entry.getId(),
                entry.getOrganizationId(),
                entry.getRecordedAt(),
                entry.getActorId(),
                entry.getActorSnapshot(),
                entry.getAction(),
                entry.getResourceType(),
                entry.getResourceId(),
                entry.getChangesDiff()
        );
    }
}
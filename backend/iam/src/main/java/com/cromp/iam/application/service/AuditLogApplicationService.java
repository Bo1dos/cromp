package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.RecordAuditLogRequest;
import com.cromp.iam.api.dto.response.AuditLogResponse;
import com.cromp.iam.api.mapper.AuditLogApiMapper;
import com.cromp.iam.api.service.AuditLogFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.domain.model.AuditLogEntry;
import com.cromp.iam.domain.repository.AuditLogRepositoryPort;
import com.cromp.iam.domain.repository.OrganizationRepositoryPort;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogApplicationService implements AuditLogFacade {

    private final AuditLogRepositoryPort auditLogRepository;
    private final OrganizationRepositoryPort organizationRepository;
    private final UserRepositoryPort userRepository;
    private final AuditLogApiMapper mapper;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;

    @Override
    public void record(RecordAuditLogRequest request) {
        AuditLogEntry entry = AuditLogEntry.record(
                request.organizationId(),
                request.actorId() != null ? request.actorId() : currentActorPort.currentUserId().orElse(null),
                request.actorSnapshot(),
                request.action(),
                request.resourceType(),
                request.resourceId(),
                request.changesDiff()
        );
        auditLogRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByOrganization(UUID orgUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = organizationRepository.findByOrgUuid(orgUuid)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"))
                .getId();
        if (!permissionCheckerPort.hasPermission(currentUserId, organizationId, "org:audit")) {
            throw new SecurityException("No permission to view organization audit log");
        }
        return auditLogRepository.findByOrganizationId(organizationId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByActor(UUID userUuid) {
        Long actorId = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        return auditLogRepository.findByActorId(actorId).stream()
                .map(mapper::toResponse)
                .toList();
    }
}

package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.RecordAuditLogRequest;
import com.cromp.iam.api.dto.response.AuditLogResponse;
import com.cromp.iam.api.mapper.AuditLogApiMapper;
import com.cromp.iam.api.service.AuditLogFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.domain.model.AuditLogEntry;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogApplicationService implements AuditLogFacade {

    private final AuditLogRepositoryPort auditLogRepository;
    private final AuditLogApiMapper mapper;
    private final CurrentActorPort currentActorPort;

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
    public List<AuditLogResponse> getByOrganization(Long organizationId) {
        return auditLogRepository.findByOrganizationId(organizationId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByActor(Long actorId) {
        return auditLogRepository.findByActorId(actorId).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
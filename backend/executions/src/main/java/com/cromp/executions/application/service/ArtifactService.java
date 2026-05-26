package com.cromp.executions.application.service;

import com.cromp.executions.api.dto.request.UploadArtifactRequest;
import com.cromp.executions.api.dto.response.ArtifactResponse;
import com.cromp.executions.api.mapper.ExecutionApiMapper;
import com.cromp.executions.application.port.ArtifactStoragePort;
import com.cromp.executions.application.port.StoredArtifact;
import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.model.exceptions.ExecutionNotFoundException;
import com.cromp.executions.domain.repository.ExecutionArtifactRepositoryPort;
import com.cromp.executions.domain.repository.ExecutionRepositoryPort;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtifactService {

    private final ExecutionRepositoryPort executionRepository;
    private final ExecutionArtifactRepositoryPort artifactRepository;
    private final ArtifactStoragePort storagePort;
    private final ExecutionApiMapper mapper;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;

    public ArtifactResponse uploadArtifact(Long organizationId, UUID execUuid,
                                           UploadArtifactRequest request,
                                           MultipartFile file) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:execute")) {
            throw new SecurityException("No permission to upload artifacts");
        }

        Execution execution = executionRepository.findByExecUuid(execUuid)
                .filter(e -> e.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new ExecutionNotFoundException(execUuid));

        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : request.kind().name().toLowerCase();

        StoredArtifact stored;
        try {
            stored = storagePort.store(
                    execution.getId(),
                    originalFilename,
                    request.contentType() != null ? request.contentType() : file.getContentType(),
                    file.getInputStream(),
                    file.getSize()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload artifact", e);
        }

        ExecutionArtifact artifact = ExecutionArtifact.create(
                execution.getId(),
                request.kind(),
                stored.storagePath(),
                stored.sizeBytes(),
                stored.checksumSha256(),
                request.contentType() != null ? request.contentType() : file.getContentType(),
                userId
        );
        artifact = artifactRepository.save(artifact);
        return mapper.toArtifactResponse(artifact);
    }

    @Transactional(readOnly = true)
    public List<ArtifactResponse> listArtifacts(Long organizationId, UUID execUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }

        Execution execution = executionRepository.findByExecUuid(execUuid)
                .filter(e -> e.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new ExecutionNotFoundException(execUuid));

        return artifactRepository.findByExecutionId(execution.getId())
                .stream()
                .map(mapper::toArtifactResponse)
                .toList();
    }
}
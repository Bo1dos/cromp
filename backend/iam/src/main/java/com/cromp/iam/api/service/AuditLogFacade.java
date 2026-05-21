package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.RecordAuditLogRequest;
import com.cromp.iam.api.dto.response.AuditLogResponse;

import java.util.List;
import java.util.UUID;

public interface AuditLogFacade {

    void record(RecordAuditLogRequest request);

    List<AuditLogResponse> getByOrganization(UUID orgUuid);
    List<AuditLogResponse> getByActor(UUID userUuid);
}
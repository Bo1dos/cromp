package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.RecordAuditLogRequest;
import com.cromp.iam.api.dto.response.AuditLogResponse;

import java.util.List;

public interface AuditLogFacade {

    void record(RecordAuditLogRequest request);

    List<AuditLogResponse> getByOrganization(Long organizationId);
    List<AuditLogResponse> getByActor(Long actorId);
}
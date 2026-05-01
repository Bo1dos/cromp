package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.response.AuditLogResponse;
import com.cromp.iam.api.service.AuditLogFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogFacade auditLogFacade;

    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'org:audit')")
    public List<AuditLogResponse> getByOrganization(@PathVariable Long organizationId) {
        return auditLogFacade.getByOrganization(organizationId);
    }

    @GetMapping("/actor/{actorId}")
    @PreAuthorize("isAuthenticated()")
    public List<AuditLogResponse> getByActor(@PathVariable Long actorId) {
        return auditLogFacade.getByActor(actorId);
    }
}
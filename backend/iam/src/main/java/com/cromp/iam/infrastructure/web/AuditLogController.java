package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.response.AuditLogResponse;
import com.cromp.iam.api.service.AuditLogFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogFacade auditLogFacade;

    @GetMapping("/organization/{orgUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<AuditLogResponse> getByOrganization(@PathVariable UUID orgUuid) {
        return auditLogFacade.getByOrganization(orgUuid);
    }

    @GetMapping("/actor/{userUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<AuditLogResponse> getByActor(@PathVariable UUID userUuid) {
        return auditLogFacade.getByActor(userUuid);
    }
}

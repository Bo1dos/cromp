package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.CreateOrganizationRequest;
import com.cromp.iam.api.dto.request.RenameOrganizationRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.api.service.OrganizationFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationFacade organizationFacade;
    private final CurrentActorPort currentActorPort;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public OrganizationResponse create(@Valid @RequestBody CreateOrganizationRequest request) {
        Long userId = currentActorPort.currentUserId().orElseThrow();
        return organizationFacade.create(request, userId);
    }

    @PutMapping("/{organizationId}/rename")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'org:update')")
    public OrganizationResponse rename(@PathVariable Long organizationId,
                                       @Valid @RequestBody RenameOrganizationRequest request) {
        return organizationFacade.rename(request);
    }

    @GetMapping("/{organizationId}")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public OrganizationResponse get(@PathVariable Long organizationId) {
        return organizationFacade.getById(organizationId);
    }

    @GetMapping("/{organizationId}/members")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<MembershipResponse> getMembers(@PathVariable Long organizationId) {
        return organizationFacade.getMembers(organizationId);
    }
}
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
import java.util.UUID;

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

    @PutMapping("/{orgUuid}/rename")
    @PreAuthorize("isAuthenticated()")
    public OrganizationResponse rename(@PathVariable UUID orgUuid,
                                       @Valid @RequestBody RenameOrganizationRequest request) {
        return organizationFacade.rename(orgUuid, request);
    }

    @GetMapping("/{orgUuid}")
    @PreAuthorize("isAuthenticated()")
    public OrganizationResponse get(@PathVariable UUID orgUuid) {
        return organizationFacade.getById(orgUuid);
    }

    @GetMapping("/{orgUuid}/members")
    @PreAuthorize("isAuthenticated()")
    public List<MembershipResponse> getMembers(@PathVariable UUID orgUuid) {
        return organizationFacade.getMembers(orgUuid);
    }
}

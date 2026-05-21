package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.AcceptInvitationRequest;
import com.cromp.iam.api.dto.request.InviteUserRequest;
import com.cromp.iam.api.dto.request.RejectInvitationRequest;
import com.cromp.iam.api.dto.request.RevokeInvitationRequest;
import com.cromp.iam.api.dto.response.InvitationCreateResponse;
import com.cromp.iam.api.dto.response.InvitationResponse;
import com.cromp.iam.api.service.InvitationFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationFacade invitationFacade;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationCreateResponse invite(@Valid @RequestBody InviteUserRequest request) {
        return invitationFacade.invite(request);
    }

    @PostMapping("/accept")
    @PreAuthorize("isAuthenticated()")
    public InvitationResponse accept(@Valid @RequestBody AcceptInvitationRequest request) {
        return invitationFacade.accept(request);
    }

    @PostMapping("/reject")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@Valid @RequestBody RejectInvitationRequest request) {
        invitationFacade.reject(request);
    }

    @PostMapping("/revoke")
    @PreAuthorize("isAuthenticated()")
    public InvitationResponse revoke(@Valid @RequestBody RevokeInvitationRequest request) {
        return invitationFacade.revoke(request);
    }

    @GetMapping("/{invitationUuid}")
    @PreAuthorize("isAuthenticated()")
    public InvitationResponse getById(@PathVariable UUID invitationUuid) {
        return invitationFacade.getById(invitationUuid);
    }

    @GetMapping("/organization/{orgUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<InvitationResponse> getByOrganization(@PathVariable UUID orgUuid) {
        return invitationFacade.getByOrganization(orgUuid);
    }
}

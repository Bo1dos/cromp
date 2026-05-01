package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.AddMembershipRequest;
import com.cromp.iam.api.dto.request.ChangeMembershipRoleRequest;
import com.cromp.iam.api.dto.request.RemoveMembershipRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.service.MembershipFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipFacade membershipFacade;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipResponse add(@Valid @RequestBody AddMembershipRequest request) {
        return membershipFacade.add(request);
    }

    @PutMapping("/{membershipId}/role")
    @PreAuthorize("isAuthenticated()")
    public MembershipResponse changeRole(@PathVariable Long membershipId,
                                         @Valid @RequestBody ChangeMembershipRoleRequest request) {
        return membershipFacade.changeRole(request);
    }

    @DeleteMapping("/{membershipId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long membershipId) {
        // Контроллер создаёт RemoveMembershipRequest из path variable
        membershipFacade.remove(new RemoveMembershipRequest(membershipId));
    }

    @GetMapping("/{membershipId}")
    @PreAuthorize("isAuthenticated()")
    public MembershipResponse getById(@PathVariable Long membershipId) {
        return membershipFacade.getById(membershipId);
    }

    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("isAuthenticated()")
    public List<MembershipResponse> getByOrganization(@PathVariable Long organizationId) {
        return membershipFacade.getByOrganization(organizationId);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<MembershipResponse> getByUser(@PathVariable Long userId) {
        return membershipFacade.getByUser(userId);
    }
}
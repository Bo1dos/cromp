package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.AddMembershipRequest;
import com.cromp.iam.api.dto.request.ChangeMembershipRoleRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.service.MembershipFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// TODO: Long убрать нахер 
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

    @PutMapping("/{membershipUuid}/role")
    @PreAuthorize("isAuthenticated()")
    public MembershipResponse changeRole(@PathVariable UUID membershipUuid,
                                        @Valid @RequestBody ChangeMembershipRoleRequest request) {
        return membershipFacade.changeRole(membershipUuid, request);
    }

    @DeleteMapping("/{membershipUuid}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID membershipUuid) {
        membershipFacade.remove(membershipUuid);
    }

    @GetMapping("/{membershipUuid}")
    @PreAuthorize("isAuthenticated()")
    public MembershipResponse getById(@PathVariable UUID membershipUuid) {
        return membershipFacade.getById(membershipUuid);
    }

    @GetMapping("/organization/{orgUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<MembershipResponse> getByOrganization(@PathVariable UUID orgUuid) {
        return membershipFacade.getByOrganization(orgUuid);
    }

    @GetMapping("/user/{userUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<MembershipResponse> getByUser(@PathVariable UUID userUuid) {
        return membershipFacade.getByUser(userUuid);
    }
}

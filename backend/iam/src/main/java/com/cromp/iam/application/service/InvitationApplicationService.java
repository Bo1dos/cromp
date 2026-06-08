package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.AcceptInvitationRequest;
import com.cromp.iam.api.dto.request.InviteUserRequest;
import com.cromp.iam.api.dto.request.RejectInvitationRequest;
import com.cromp.iam.api.dto.request.RevokeInvitationRequest;
import com.cromp.iam.api.dto.response.InvitationCreateResponse;
import com.cromp.iam.api.dto.response.InvitationResponse;
import com.cromp.iam.api.mapper.InvitationApiMapper;
import com.cromp.iam.api.service.InvitationFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.domain.model.Invitation;
import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.enums.InvitationStatus;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.common.event.integration.publisher.DomainEventPublisher;
import com.cromp.common.application.port.EmailSenderPort;
import com.cromp.common.event.domain.invitation.InvitationCreatedEvent;
import com.cromp.common.event.domain.invitation.InvitationAcceptedEvent;
import com.cromp.common.event.domain.invitation.InvitationRejectedEvent;
import com.cromp.common.event.domain.invitation.InvitationRevokedEvent;
import com.cromp.iam.domain.repository.InvitationRepositoryPort;
import com.cromp.iam.domain.repository.MembershipRepositoryPort;
import com.cromp.iam.domain.repository.OrganizationRepositoryPort;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationApplicationService implements InvitationFacade {

    private final InvitationRepositoryPort invitationRepository;
    private final OrganizationRepositoryPort organizationRepository;
    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final MembershipRepositoryPort membershipRepository;
    private final InvitationApiMapper invitationMapper;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;
    private final DomainEventPublisher domainEventPublisher;
    private final EmailSenderPort emailSenderPort;

    @Value("${app.frontend-url:http://localhost:3001}")
    private String frontendUrl;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public InvitationCreateResponse invite(InviteUserRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("No organization selected"));

        if (!permissionCheckerPort.hasPermission(currentUserId, organizationId, "org:invite")) {
            throw new SecurityException("No permission to invite users to this organization");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new DomainException("User not found"));
        String inviterName = currentUser.getDisplayName() != null
                ? currentUser.getDisplayName() : currentUser.getEmail();
        var org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new DomainException("Organization not found"));

        String token = generateToken();
        String tokenHash = hashToken(token);

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new DomainException("Role not found"));
        Instant expiresAt = request.expiresAt() != null
                ? request.expiresAt()
                : Instant.now().plusSeconds(7 * 24 * 3600);


        Invitation invitation = Invitation.create(
                organizationId,
                request.email(),
                tokenHash,
                role.getId(),
                currentUserId,
                expiresAt
        );

        invitation = invitationRepository.save(invitation);

        domainEventPublisher.publish(new InvitationCreatedEvent(
                invitation.getInvitationUuid(),
                organizationId,
                invitation.getEmail(),
                role.getName().toString(),
                currentUserId,
                inviterName
        ));

        // Отправка email приглашённому
        try {
            String acceptUrl = frontendUrl + "/accept?token=" + token;
            String subject = inviterName + " invited you to " + org.getName();
            String htmlBody = "<html><body style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;\">"
                    + "<h2>You've been invited!</h2>"
                    + "<p><strong>" + inviterName + "</strong> invited you to join "
                    + "<strong>" + org.getName() + "</strong> on Cron-as-a-Service.</p>"
                    + "<p>Your role: <strong>" + role.getName() + "</strong></p>"
                    + "<p>This invitation expires: " + invitation.getExpiresAt() + "</p>"
                    + "<a href=\"" + acceptUrl + "\" style=\"display:inline-block;padding:12px 24px;"
                    + "background:#1677ff;color:#fff;text-decoration:none;border-radius:6px;\">Accept Invitation</a>"
                    + "<hr style=\"margin-top:30px;border-color:#eee;\">"
                    + "<p style=\"color:#999;font-size:12px;\">This is an automated message from Cron-as-a-Service.</p>"
                    + "</body></html>";
            emailSenderPort.send(invitation.getEmail(), subject, htmlBody);
        } catch (Exception e) {
            // Email failure is non-critical — invitation is already saved
        }

        return new InvitationCreateResponse(
                invitationMapper.toResponse(invitation, role.getName().toString()),
                token
        );
    }

    @Override
    public InvitationResponse accept(AcceptInvitationRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Invitation invitation = findInvitationByToken(request.token());

        ensurePendingAndNotExpired(invitation);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new DomainException("User not found"));

        if (!currentUser.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new SecurityException("Invitation is not addressed to current user");
        }

        invitation.accept();
        invitation = invitationRepository.save(invitation);

        domainEventPublisher.publish(new InvitationAcceptedEvent(
                invitation.getInvitationUuid(),
                invitation.getOrganizationId(),
                currentUserId,
                currentUser.getEmail(),
                invitation.getInvitedBy()
        ));

        Role role = roleRepository.findById(invitation.getRoleId())
                .orElseThrow(() -> new DomainException("Role not found"));

        Membership membership = Membership.join(currentUserId, invitation.getOrganizationId(), role.getId());
        membershipRepository.save(membership);

        return invitationMapper.toResponse(invitation, role.getName().toString());
    }

    @Override
    public void reject(RejectInvitationRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));


        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new DomainException("User not found"));
        Invitation invitation = findInvitationByToken(request.token());

        ensurePendingAndNotExpired(invitation);

        if (!currentUser.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new SecurityException("Invitation is not addressed to current user");
        }

        domainEventPublisher.publish(new InvitationRejectedEvent(
                invitation.getInvitationUuid(),
                invitation.getOrganizationId(),
                currentUserId,
                invitation.getInvitedBy()
        ));

        invitationRepository.deleteById(invitation.getId());
    }

    @Override
    public InvitationResponse revoke(RevokeInvitationRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
                
        Invitation invitation = invitationRepository.findByInvitationUuid(request.invitationUuid())
                .orElseThrow(() -> new DomainException("Invitation not found"));

        if (!permissionCheckerPort.hasPermission(currentUserId, invitation.getOrganizationId(), "org:invite")) {
            throw new SecurityException("No permission to revoke invitations in this organization");
        }

        invitation.revoke();
        invitation = invitationRepository.save(invitation);

        domainEventPublisher.publish(new InvitationRevokedEvent(
                invitation.getInvitationUuid(),
                invitation.getOrganizationId(),
                currentUserId,
                invitation.getEmail()
        ));

        Role role = roleRepository.findById(invitation.getRoleId()).orElse(null);
        return invitationMapper.toResponse(invitation, role != null ? role.getName().toString() : null);
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationResponse getById(UUID invitationUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Invitation invitation = invitationRepository.findByInvitationUuid(invitationUuid)
                .orElseThrow(() -> new DomainException("Invitation not found"));

        if (!permissionCheckerPort.isMember(currentUserId, invitation.getOrganizationId())) {
            throw new SecurityException("Not a member of this organization");
        }

        Role role = roleRepository.findById(invitation.getRoleId()).orElse(null);
        return invitationMapper.toResponse(invitation, role != null ? role.getName().toString() : null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getByOrganization(UUID orgUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = organizationRepository.findByOrgUuid(orgUuid)
                .orElseThrow(() -> new DomainException("Organization not found"))
                .getId();

        if (!permissionCheckerPort.isMember(currentUserId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }

        return invitationRepository.findByOrganizationId(organizationId).stream()
                .map(invitation -> {
                    Role role = roleRepository.findById(invitation.getRoleId()).orElse(null);
                    return invitationMapper.toResponse(invitation, role != null ? role.getName().toString() : null);
                })
                .toList();
    }

    @Override
    public InvitationResponse acceptByUuid(UUID invitationUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Invitation invitation = invitationRepository.findByInvitationUuid(invitationUuid)
                .orElseThrow(() -> new DomainException("Invitation not found"));

        ensurePendingAndNotExpired(invitation);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new DomainException("User not found"));

        if (!currentUser.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new SecurityException("Invitation is not addressed to current user");
        }

        invitation.accept();
        invitation = invitationRepository.save(invitation);

        domainEventPublisher.publish(new InvitationAcceptedEvent(
                invitation.getInvitationUuid(),
                invitation.getOrganizationId(),
                currentUserId,
                currentUser.getEmail(),
                invitation.getInvitedBy()
        ));

        Role role = roleRepository.findById(invitation.getRoleId())
                .orElseThrow(() -> new DomainException("Role not found"));

        Membership membership = Membership.join(currentUserId, invitation.getOrganizationId(), role.getId());
        membershipRepository.save(membership);

        return invitationMapper.toResponse(invitation, role.getName().toString());
    }

    @Override
    public void rejectByUuid(UUID invitationUuid) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Invitation invitation = invitationRepository.findByInvitationUuid(invitationUuid)
                .orElseThrow(() -> new DomainException("Invitation not found"));

        ensurePendingAndNotExpired(invitation);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new DomainException("User not found"));

        if (!currentUser.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw new SecurityException("Invitation is not addressed to current user");
        }

        domainEventPublisher.publish(new InvitationRejectedEvent(
                invitation.getInvitationUuid(),
                invitation.getOrganizationId(),
                currentUserId,
                invitation.getInvitedBy()
        ));

        invitationRepository.deleteById(invitation.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getMyInvitations() {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new DomainException("User not found"));
        return invitationRepository.findByEmail(currentUser.getEmail()).stream()
                .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                .map(inv -> {
                    Role role = roleRepository.findById(inv.getRoleId()).orElse(null);
                    return invitationMapper.toResponse(inv, role != null ? role.getName().toString() : null);
                })
                .toList();
    }

    private Invitation findInvitationByToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        return invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new DomainException("Invalid invitation token"));
    }

    private void ensurePendingAndNotExpired(Invitation invitation) {
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new DomainException("Invitation is no longer valid");
        }
        if (Instant.now().isAfter(invitation.getExpiresAt())) {
            invitation.expire();
            invitationRepository.save(invitation);
            throw new DomainException("Invitation has expired");
        }
    }


    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }
}

package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.AcceptInvitationRequest;
import com.cromp.iam.api.dto.request.InviteUserRequest;
import com.cromp.iam.api.dto.request.RejectInvitationRequest;
import com.cromp.iam.api.dto.request.RevokeInvitationRequest;
import com.cromp.iam.api.dto.response.InvitationResponse;
import com.cromp.iam.api.mapper.InvitationApiMapper;
import com.cromp.iam.api.service.InvitationFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.iam.domain.model.*;
import com.cromp.iam.domain.model.enums.InvitationStatus;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.*;
import lombok.RequiredArgsConstructor;

import org.apache.commons.codec.digest.DigestUtils;
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

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public InvitationResponse invite(InviteUserRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        // Проверяем право на приглашение
        if (!permissionCheckerPort.hasPermission(currentUserId, request.organizationId(), "org:invite")) {
            throw new SecurityException("No permission to invite users to this organization");
        }

        // Проверяем, что организация существует
        organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new DomainException("Organization not found"));

        // Генерируем токен приглашения
        String token = generateToken();
        String tokenHash = hashToken(token);

        Instant expiresAt = request.expiresAt() != null ? request.expiresAt() : Instant.now().plusSeconds(7 * 24 * 3600); // 7 дней по умолчанию

        Invitation invitation = Invitation.create(
                request.organizationId(),
                request.email(),
                tokenHash,
                request.roleId(),
                request.invitedBy(),
                expiresAt
        );
        invitation = invitationRepository.save(invitation);

        // Возвращаем ответ с оригинальным токеном (только один раз!)
        return invitationMapper.toResponse(invitation, null, token); // имя роли можно зарезолвить позже
    }

    @Override
    public InvitationResponse accept(AcceptInvitationRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        // Найти приглашение по токену
        String tokenHash = hashToken(request.token());
        Invitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new DomainException("Invalid invitation token"));

        // Проверить, что приглашение ещё ожидает ответа
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new DomainException("Invitation is no longer valid");
        }

        // Проверить срок действия
        if (Instant.now().isAfter(invitation.getExpiresAt())) {
            invitation.expire();
            invitationRepository.save(invitation);
            throw new DomainException("Invitation has expired");
        }

        // Принять приглашение (бизнес-логика внутри доменной модели)
        invitation.accept();
        invitation = invitationRepository.save(invitation);

        // Создать членство для пользователя
        Role role = roleRepository.findById(invitation.getRoleId())
                .orElseThrow(() -> new DomainException("Role not found"));
        Membership membership = Membership.join(currentUserId, invitation.getOrganizationId(), role.getId());
        membershipRepository.save(membership);

        return invitationMapper.toResponse(invitation, role.getName().toString(), null);
    }

    @Override
    public InvitationResponse reject(RejectInvitationRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        String tokenHash = hashToken(request.token());
        Invitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new DomainException("Invalid invitation token"));

        // Проверяем, что приглашение в статусе PENDING
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new DomainException("Invitation is no longer valid");
        }

        // Просто удаляем (или можно ввести статус REJECTED, но у нас есть REVOKED — тут лучше просто удалить)
        invitationRepository.deleteById(invitation.getId());
        return null; // или вернуть пустой ответ
    }

    @Override
    public InvitationResponse revoke(RevokeInvitationRequest request) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Invitation invitation = invitationRepository.findById(request.invitationId())
                .orElseThrow(() -> new DomainException("Invitation not found"));

        // Проверяем право на отзыв приглашения (org:invite или org:update)
        if (!permissionCheckerPort.hasPermission(currentUserId, invitation.getOrganizationId(), "org:invite")) {
            throw new SecurityException("No permission to revoke invitations in this organization");
        }

        invitation.revoke();
        invitation = invitationRepository.save(invitation);
        return invitationMapper.toResponse(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationResponse getById(Long invitationId) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new DomainException("Invitation not found"));

        // Доступ только членам организации
        if (!permissionCheckerPort.isMember(currentUserId, invitation.getOrganizationId())) {
            throw new SecurityException("Not a member of this organization");
        }

        return invitationMapper.toResponse(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationResponse getByTokenHash(String tokenHash) {
        // Неясно...
        // Этот метод используется для публичного просмотра статуса приглашения по токену? 
        // Пока без аутентификации — разрешаем только чтение неаутентифицированным (например, страница принятия приглашения)
        // Но осторожно: если показывать информацию об организации, то может быть утечка.
        // Лучше сделать доступ по токену только для проверки существования, без раскрытия деталей. 
        // Здесь просто возвращаем информацию, но ограничим поля.
        // Пока реализуем обычный поиск (защитим на уровне контроллера)
        Invitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new DomainException("Invitation not found"));
        return invitationMapper.toResponse(invitation);
    }

    @Override
    public InvitationResponse getByRawToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        return invitationRepository.findByTokenHash(tokenHash)
                .map(invitationMapper::toResponse)
                .orElseThrow(() -> new DomainException("Invitation not found"));
}

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getByOrganization(Long organizationId) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));

        if (!permissionCheckerPort.isMember(currentUserId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }

        return invitationRepository.findByOrganizationId(organizationId).stream()
                .map(invitationMapper::toResponse)
                .toList();
    }

    // Утилитные методы для токенов
    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }
}
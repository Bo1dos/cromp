package com.cromp.iam.domain.repository;

import com.cromp.iam.domain.model.Invitation;
import com.cromp.iam.domain.model.enums.InvitationStatus;

import java.util.List;
import java.util.Optional;

public interface InvitationRepositoryPort {
    Invitation save(Invitation invitation);
    Optional<Invitation> findById(Long id);
    Optional<Invitation> findByTokenHash(String tokenHash);
    List<Invitation> findByOrganizationId(Long organizationId);
    List<Invitation> findByOrganizationIdAndStatus(Long organizationId, InvitationStatus status);
    void deleteById(Long id);
}
package com.cromp.iam.infrastructure.persistence.adapter;

import com.cromp.iam.domain.model.Invitation;
import com.cromp.iam.domain.model.enums.InvitationStatus;
import com.cromp.iam.domain.repository.InvitationRepositoryPort;
import com.cromp.iam.infrastructure.persistence.jpa.repository.InvitationJpaRepository;
import com.cromp.iam.infrastructure.persistence.mapper.InvitationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class InvitationRepositoryAdapter implements InvitationRepositoryPort {

    private final InvitationJpaRepository repository;
    private final InvitationPersistenceMapper mapper;

    @Override
    public Invitation save(Invitation invitation) {
        return mapper.toDomain(repository.save(mapper.toJpa(invitation)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invitation> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invitation> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invitation> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationId(organizationId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invitation> findByOrganizationIdAndStatus(Long organizationId, InvitationStatus status) {
        return repository.findByOrganizationIdAndStatus(organizationId, status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Invitation> findByInvitationUuid(UUID invitationUuid) {
        return repository.findByInvitationUuid(invitationUuid).map(mapper::toDomain);
    }
}

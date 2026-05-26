package com.cromp.iam.infrastructure.persistence.adapter;

import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.domain.repository.MembershipRepositoryPort;
import com.cromp.iam.infrastructure.persistence.jpa.repository.MembershipJpaRepository;
import com.cromp.iam.infrastructure.persistence.mapper.MembershipPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class MembershipRepositoryAdapter implements MembershipRepositoryPort {

    private final MembershipJpaRepository repository;
    private final MembershipPersistenceMapper mapper;

    @Override
    public Membership save(Membership membership) {
        return mapper.toDomain(repository.save(mapper.toJpa(membership)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Membership> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Membership> findByUserIdAndOrganizationId(Long userId, Long organizationId) {
        return repository.findByUserIdAndOrganizationId(userId, organizationId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Membership> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationId(organizationId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Membership> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Membership> findByMembershipUuid(UUID membershipUuid) {
        return repository.findByMembershipUuid(membershipUuid).map(mapper::toDomain);
    }
}

package com.cromp.iam.domain.repository;

import com.cromp.iam.domain.model.Membership;

import java.util.List;
import java.util.Optional;

public interface MembershipRepositoryPort {
    Membership save(Membership membership);
    Optional<Membership> findById(Long id);
    Optional<Membership> findByUserIdAndOrganizationId(Long userId, Long organizationId);
    List<Membership> findByOrganizationId(Long organizationId);
    List<Membership> findByUserId(Long userId);
    void deleteById(Long id);
}
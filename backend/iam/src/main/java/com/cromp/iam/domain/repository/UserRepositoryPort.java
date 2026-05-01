package com.cromp.iam.domain.repository;

import com.cromp.iam.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUserUuid(UUID userUuid);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
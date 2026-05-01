package com.cromp.iam.infrastructure.persistence.mapper;

import com.cromp.iam.domain.model.User;
import com.cromp.iam.infrastructure.persistence.jpa.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserJpaEntity toJpa(User user) {
        return UserJpaEntity.builder()
                .id(user.getId())
                .userUuid(user.getUserUuid())
                .email(user.getEmail())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .displayName(user.getDisplayName())
                .passwordHash(user.getPasswordHash())
                .profile(user.getProfile())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    public User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                entity.getId(),
                entity.getUserUuid(),
                entity.getEmail(),
                entity.getLastName(),
                entity.getFirstName(),
                entity.getMiddleName(),
                entity.getDisplayName(),
                entity.getPasswordHash(),
                entity.getProfile(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
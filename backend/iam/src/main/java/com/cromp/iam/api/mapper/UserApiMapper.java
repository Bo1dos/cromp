package com.cromp.iam.api.mapper;

import com.cromp.iam.api.dto.response.UserResponse;
import com.cromp.iam.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserApiMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserUuid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName(),
                user.getDisplayName(),
                user.getProfile(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt()
        );
    }
}
package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.ChangeUserEmailRequest;
import com.cromp.iam.api.dto.request.ChangeUserPasswordRequest;
import com.cromp.iam.api.dto.request.UpdateUserProfileRequest;
import com.cromp.iam.api.dto.response.UserResponse;
import com.cromp.iam.api.mapper.UserApiMapper;
import com.cromp.iam.api.service.UserFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PasswordHasherPort;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService implements UserFacade {

    private final UserRepositoryPort userRepository;
    private final UserApiMapper userMapper;
    private final PasswordHasherPort passwordHasher;
    private final CurrentActorPort currentActorPort;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID userUuid) {
        User user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new DomainException("User not found"));
        ensureCurrentUserMatches(user.getId());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("User not found"));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(UUID userUuid, UpdateUserProfileRequest request) {
        User user = resolveCurrentUser(userUuid);
        user.updateName(request.firstName(), request.lastName(), request.middleName(), request.displayName());
        if (request.profile() != null) {
            user.updateProfile(request.profile());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void changeEmail(UUID userUuid, ChangeUserEmailRequest request) {
        User user = resolveCurrentUser(userUuid);
        if (userRepository.existsByEmail(request.newEmail())) {
            throw new DomainException("Email already in use");
        }
        user.changeEmail(request.newEmail());
        userRepository.save(user);
    }

    @Override
    public void changePassword(UUID userUuid, ChangeUserPasswordRequest request) {
        User user = resolveCurrentUser(userUuid);
        if (user.getPasswordHash() == null || !passwordHasher.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new DomainException("Old password is incorrect");
        }
        user.changePasswordHash(passwordHasher.hash(request.newPassword()));
        userRepository.save(user);
    }

    private User resolveCurrentUser(UUID userUuid) {
        User user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new DomainException("User not found"));
        ensureCurrentUserMatches(user.getId());
        return user;
    }

    private void ensureCurrentUserMatches(Long userId) {
        Long currentUserId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!currentUserId.equals(userId)) {
            throw new SecurityException("You can only access your own user");
        }
    }
}

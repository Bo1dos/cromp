package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.ChangeUserEmailRequest;
import com.cromp.iam.api.dto.request.ChangeUserPasswordRequest;
import com.cromp.iam.api.dto.request.UpdateUserProfileRequest;
import com.cromp.iam.api.dto.response.UserResponse;
import com.cromp.iam.api.mapper.UserApiMapper;
import com.cromp.iam.api.service.UserFacade;
import com.cromp.iam.application.port.PasswordHasherPort;
import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService implements UserFacade {

    private final UserRepositoryPort userRepository;
    private final UserApiMapper userMapper;
    private final PasswordHasherPort passwordHasher;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found"));
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
    public UserResponse updateProfile(UpdateUserProfileRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new DomainException("User not found"));

        user.updateName(request.firstName(), request.lastName(), request.middleName(), request.displayName());
        if (request.profile() != null) {
            user.updateProfile(request.profile());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void changeEmail(ChangeUserEmailRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new DomainException("User not found"));
        // Проверка уникальности нового email
        if (userRepository.existsByEmail(request.newEmail())) {
            throw new DomainException("Email already in use");
        }
        user.changeEmail(request.newEmail());
        userRepository.save(user);
    }

    @Override
    public void changePassword(ChangeUserPasswordRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new DomainException("User not found"));
        if (user.getPasswordHash() == null || !passwordHasher.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new DomainException("Old password is incorrect");
        }
        String newHash = passwordHasher.hash(request.newPassword());
        user.changePasswordHash(newHash);
        userRepository.save(user);
    }
}
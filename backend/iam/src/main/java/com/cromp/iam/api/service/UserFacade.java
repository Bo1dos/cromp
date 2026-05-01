package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.ChangeUserEmailRequest;
import com.cromp.iam.api.dto.request.ChangeUserPasswordRequest;
import com.cromp.iam.api.dto.request.UpdateUserProfileRequest;
import com.cromp.iam.api.dto.response.UserResponse;

public interface UserFacade {

    UserResponse getById(Long userId);
    UserResponse getByEmail(String email);

    UserResponse updateProfile(UpdateUserProfileRequest request);

    void changeEmail(ChangeUserEmailRequest request);
    void changePassword(ChangeUserPasswordRequest request);
}
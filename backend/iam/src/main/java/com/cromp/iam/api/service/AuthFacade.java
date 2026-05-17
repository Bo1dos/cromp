package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.LoginRequest;
import com.cromp.iam.api.dto.request.RegisterRequest;
import com.cromp.iam.api.dto.request.SelectOrganizationRequest;
import com.cromp.iam.api.dto.response.AuthResponse;

public interface AuthFacade {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse selectOrganization(SelectOrganizationRequest request);
}
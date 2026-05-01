package com.cromp.iam.api.mapper;

import com.cromp.iam.api.dto.response.AuthResponse;
import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.api.dto.response.UserResponse;
import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthApiMapper {

    private final UserApiMapper userApiMapper;
    private final OrganizationApiMapper organizationApiMapper;

    public AuthApiMapper(UserApiMapper userApiMapper, OrganizationApiMapper organizationApiMapper) {
        this.userApiMapper = userApiMapper;
        this.organizationApiMapper = organizationApiMapper;
    }

    public AuthResponse toLoginResponse(String accessToken, User user, List<Organization> organizations) {
        UserResponse userResponse = userApiMapper.toResponse(user);
        List<OrganizationResponse> orgs = organizations.stream()
                .map(organizationApiMapper::toResponse)
                .toList();
        return new AuthResponse("Bearer", accessToken, userResponse, orgs, null);
    }

    public AuthResponse toSelectResponse(String accessToken, User user, Organization organization) {
        UserResponse userResponse = userApiMapper.toResponse(user);
        OrganizationResponse orgResponse = organizationApiMapper.toResponse(organization);
        return new AuthResponse("Bearer", accessToken, userResponse, null, orgResponse);
    }
}
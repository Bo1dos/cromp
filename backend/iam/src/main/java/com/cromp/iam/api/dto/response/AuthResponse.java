package com.cromp.iam.api.dto.response;

import java.util.List;

public record AuthResponse(
        String tokenType,
        String accessToken,
        UserResponse user,
        List<OrganizationResponse> organizations, // список доступных организаций (может быть пустым)
        OrganizationResponse activeOrganization   // null при логине, заполняется при выборе
) { }
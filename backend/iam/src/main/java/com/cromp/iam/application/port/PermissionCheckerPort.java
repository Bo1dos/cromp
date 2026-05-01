package com.cromp.iam.application.port;

public interface PermissionCheckerPort {
    boolean hasPermission(Long userId, Long organizationId, String permission);
    boolean isMember(Long userId, Long organizationId);
}
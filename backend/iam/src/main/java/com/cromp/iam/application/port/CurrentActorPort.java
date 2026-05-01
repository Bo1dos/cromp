package com.cromp.iam.application.port;

import java.util.Map;
import java.util.Optional;

public interface CurrentActorPort {
    Optional<Long> currentUserId();
    Optional<Long> currentOrganizationId();
    Optional<Map<String, Object>> currentActorSnapshot();
}
package com.cromp.jobs.application.port;

import java.util.Map;

public interface AuditPort {
    void record(String action, Long organizationId, Long actorId,
                String resourceType, Long resourceId, Map<String, Object> changesDiff);
}
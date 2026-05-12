package com.cromp.schedules.application.port;

import java.util.Map;

// TODO: переписать под 1 атрибут
public interface AuditPort {
    void record(String action, Long organizationId, Long actorId,
                String resourceType, Long resourceId, Map<String, Object> changesDiff);
}
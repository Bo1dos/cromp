package com.cromp.iam.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Порт для резолва внешнего UUID организации во внутренний Long ID.
 * Используется контроллерами других модулей (executions, analytics, ...)
 * для преобразования UUID в пути в Long для сервисного слоя.
 */
public interface OrganizationLookupPort {
    /**
     * Возвращает внутренний ID организации по её UUID.
     *
     * @param orgUuid внешний UUID организации
     * @return Optional с Long ID, либо empty если организация не найдена
     */
    Optional<Long> resolveOrganizationId(UUID orgUuid);
}

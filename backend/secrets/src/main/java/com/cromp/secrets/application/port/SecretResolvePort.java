package com.cromp.secrets.application.port;

import java.util.Map;
import java.util.UUID;

/**
 * Порт для orchestrator'а: получить расшифрованные значения секретов по их UUID.
 *
 * Контракт намеренно минимален: на входе — список UUID секретов,
 * на выходе — Map (secretUuid -> plaintext). Orchestrator сам сопоставит
 * значения с именами переменных окружения (envName) из JobSecretRef.
 *
 * Интерфейс живёт в application-слое модуля secrets,
 * реализация — в infrastructure-слое того же модуля.
 * Orchestrator зависит только от этого интерфейса — граница не нарушается.
 */
public interface SecretResolvePort {

    /**
     * Возвращает расшифрованные значения активных версий запрошенных секретов.
     *
     * @param organizationId идентификатор организации — защита от межорганизационного доступа
     * @param secretIds      UUID секретов, значения которых нужны
     * @return Map: secretUuid -> plaintext-значение активной версии
     * @throws com.cromp.secrets.domain.model.exceptions.SecretNotFoundException
     *         если любой из secretIds не существует или не принадлежит организации
     */
    Map<UUID, String> resolveSecrets(Long organizationId, java.util.List<UUID> secretIds);
}
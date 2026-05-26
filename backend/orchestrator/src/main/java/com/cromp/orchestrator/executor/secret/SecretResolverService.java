package com.cromp.orchestrator.executor.secret;

import com.cromp.jobs.domain.model.JobSecretRef;
import com.cromp.secrets.application.port.SecretResolvePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Разрешает список {@link JobSecretRef} в Map envName → plaintext.
 *
 * Оркестратор передаёт эту Map в {@code HttpTaskAdapter} как заголовки или
 * подстановки в тело запроса. Сами значения секретов нигде не логируются.
 *
 * Логика:
 *   1. Собрать уникальные secretId из списка ссылок.
 *   2. Запросить расшифровку у {@link SecretResolvePort} одним батчем.
 *   3. Сопоставить UUID → envName, вернуть итоговую Map.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecretResolverService {

    private final SecretResolvePort secretResolvePort;

    /**
     * @param organizationId для проверки принадлежности секретов
     * @param secretRefs     список ссылок из JobConfig
     * @return Map: envName → plaintext-значение
     */
    public Map<String, String> resolve(Long organizationId, List<JobSecretRef> secretRefs) {
        if (secretRefs == null || secretRefs.isEmpty()) {
            return Map.of();
        }

        List<UUID> secretIds = secretRefs.stream()
                .map(JobSecretRef::secretId)
                .distinct()
                .toList();

        // Один батч-запрос к secrets-модулю — не N отдельных вызовов
        Map<UUID, String> resolved = secretResolvePort.resolveSecrets(organizationId, secretIds);

        // Сопоставляем UUID → envName
        Map<String, String> result = secretRefs.stream()
                .filter(ref -> resolved.containsKey(ref.secretId()))
                .collect(Collectors.toMap(
                        JobSecretRef::envName,
                        ref -> resolved.get(ref.secretId()),
                        // При дублях envName берём первый — конфиг Job не должен допускать такого
                        (first, second) -> first
                ));

        log.debug("Resolved {}/{} secret(s) for organizationId={}",
                result.size(), secretRefs.size(), organizationId);

        return result;
    }
}
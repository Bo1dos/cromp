package com.cromp.secrets.infrastructure.port;

import com.cromp.secrets.application.port.SecretResolvePort;
import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.SecretVersion;
import com.cromp.secrets.domain.model.exceptions.SecretNotFoundException;
import com.cromp.secrets.domain.repository.SecretRepositoryPort;
import com.cromp.secrets.domain.repository.SecretVersionRepositoryPort;
import com.cromp.secrets.domain.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация {@link SecretResolvePort}.
 *
 * Для каждого запрошенного UUID:
 *   1. Находит Secret в репозитории (с проверкой принадлежности организации).
 *   2. Загружает активную версию ({@code SecretVersion.active = true}).
 *   3. Расшифровывает {@code valueCipher} через {@link EncryptionService}.
 *
 * Транзакция readOnly: мы только читаем, запись не нужна.
 * Расшифровка происходит в памяти — cipher не покидает процесса.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecretResolvePortImpl implements SecretResolvePort {

    private final SecretRepositoryPort secretRepository;
    private final SecretVersionRepositoryPort versionRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> resolveSecrets(Long organizationId, List<UUID> secretIds) {
        Map<UUID, String> result = new LinkedHashMap<>(secretIds.size());

        for (UUID secretId : secretIds) {
            String plaintext = resolveOne(organizationId, secretId);
            result.put(secretId, plaintext);
        }

        log.debug("Resolved {} secret(s) for organizationId={}", result.size(), organizationId);
        return result;
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private String resolveOne(Long organizationId, UUID secretUuid) {
        Secret secret = secretRepository.findBySecretUuid(secretUuid)
                .orElseThrow(() -> new SecretNotFoundException(
                        "Secret not found: " + secretUuid));

        // Проверка принадлежности организации — защита от межорганизационного доступа
        if (!secret.getOrganizationId().equals(organizationId)) {
            throw new SecretNotFoundException(
                    "Secret not found: " + secretUuid);
        }

        SecretVersion activeVersion = versionRepository.findActiveVersion(secret.getId())
                .orElseThrow(() -> new SecretNotFoundException(
                        "No active version for secret: " + secretUuid));

        byte[] plainBytes = encryptionService.decrypt(activeVersion.getValueCipher());
        return new String(plainBytes, StandardCharsets.UTF_8);
    }
}
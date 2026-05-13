package com.cromp.secrets.domain.model;

import com.cromp.secrets.domain.model.enums.SecretScope;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretTest {
    @Test
    void createTrimsNameAndSetsScope() {
        Secret secret = Secret.create(UUID.randomUUID(), 10L, " api-token ", SecretScope.JOB, " desc ");

        assertThat(secret.getName()).isEqualTo("api-token");
        assertThat(secret.getScope()).isEqualTo(SecretScope.JOB);
        assertThat(secret.isDeleted()).isFalse();
    }

    @Test
    void deletedSecretCannotBeRenamed() {
        Secret secret = Secret.create(UUID.randomUUID(), 10L, "api-token", SecretScope.JOB, null);
        secret.delete();

        assertThatThrownBy(() -> secret.rename("new-name"))
                .isInstanceOf(IllegalStateException.class);
    }
}

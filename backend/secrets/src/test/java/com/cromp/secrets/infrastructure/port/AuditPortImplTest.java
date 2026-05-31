package com.cromp.secrets.infrastructure.port;

import com.cromp.iam.domain.model.AuditLogEntry;
import com.cromp.iam.domain.repository.AuditLogRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditPortImplTest {

    @Mock
    private AuditLogRepositoryPort auditLogRepository;

    private AuditPortImpl port;

    @BeforeEach
    void setUp() {
        port = new AuditPortImpl(auditLogRepository);
    }

    @Test
    void shouldRecordAuditLogEntryWhenCalled() {
        Map<String, Object> changesDiff = Map.of("field", "before -> after");

        port.record("SECRET.CREATE", 22L, 11L, "secrets", 100L, changesDiff);

        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogEntry saved = captor.getValue();
        assertThat(saved.getOrganizationId()).isEqualTo(22L);
        assertThat(saved.getActorId()).isEqualTo(11L);
        assertThat(saved.getAction()).isEqualTo("SECRET.CREATE");
        assertThat(saved.getResourceType()).isEqualTo("secrets");
        assertThat(saved.getResourceId()).isEqualTo(100L);
        assertThat(saved.getChangesDiff()).isEqualTo(changesDiff);
        assertThat(saved.getRecordedAt()).isNotNull();
    }
}

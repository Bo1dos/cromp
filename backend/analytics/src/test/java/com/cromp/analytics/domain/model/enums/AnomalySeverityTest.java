package com.cromp.analytics.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnomalySeverityTest {

    @Test
    void shouldHaveAllExpectedValues() {
        assertThat(AnomalySeverity.values())
                .containsExactly(AnomalySeverity.LOW, AnomalySeverity.MEDIUM,
                        AnomalySeverity.HIGH, AnomalySeverity.CRITICAL);
    }

    @Test
    void shouldParseFromNameCaseInsensitive() {
        assertThat(AnomalySeverity.valueOf("LOW")).isEqualTo(AnomalySeverity.LOW);
        assertThat(AnomalySeverity.valueOf("MEDIUM")).isEqualTo(AnomalySeverity.MEDIUM);
        assertThat(AnomalySeverity.valueOf("HIGH")).isEqualTo(AnomalySeverity.HIGH);
        assertThat(AnomalySeverity.valueOf("CRITICAL")).isEqualTo(AnomalySeverity.CRITICAL);
    }
}

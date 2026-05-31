package com.cromp.analytics.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalyticsPeriodTest {

    @Test
    void shouldReturnCorrectDaysForAllPeriods() {
        assertThat(AnalyticsPeriod.ONE_DAY.getDays()).isEqualTo(1);
        assertThat(AnalyticsPeriod.SEVEN_DAYS.getDays()).isEqualTo(7);
        assertThat(AnalyticsPeriod.THIRTY_DAYS.getDays()).isEqualTo(30);
        assertThat(AnalyticsPeriod.NINETY_DAYS.getDays()).isEqualTo(90);
    }

    @ParameterizedTest
    @CsvSource({
            "1d,  ONE_DAY",
            "7d,  SEVEN_DAYS",
            "30d, THIRTY_DAYS",
            "90d, NINETY_DAYS"
    })
    void shouldResolveAlias(String alias, AnalyticsPeriod expected) {
        assertThat(AnalyticsPeriod.fromAlias(alias)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"1D", "7D", "30D", "90D"})
    void shouldBeCaseInsensitive(String alias) {
        assertThat(AnalyticsPeriod.fromAlias(alias)).isNotNull();
    }

    @Test
    void shouldThrowWhenAliasIsInvalid() {
        assertThatThrownBy(() -> AnalyticsPeriod.fromAlias("5d"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown period")
                .hasMessageContaining("5d")
                .hasMessageContaining("1d")
                .hasMessageContaining("7d")
                .hasMessageContaining("30d")
                .hasMessageContaining("90d");
    }

    @Test
    void shouldThrowWhenAliasIsNull() {
        assertThatThrownBy(() -> AnalyticsPeriod.fromAlias(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenAliasIsEmpty() {
        assertThatThrownBy(() -> AnalyticsPeriod.fromAlias(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown period");
    }
}

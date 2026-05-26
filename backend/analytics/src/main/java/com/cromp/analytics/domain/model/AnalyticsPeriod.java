package com.cromp.analytics.domain.model;

/**
 * Период агрегации для сводной статистики.
 * Значение {@code days} используется в SQL-запросах как INTERVAL.
 */
public enum AnalyticsPeriod {

    ONE_DAY(1, "1d"),
    SEVEN_DAYS(7, "7d"),
    THIRTY_DAYS(30, "30d"),
    NINETY_DAYS(90, "90d");

    private final int days;
    private final String alias;

    AnalyticsPeriod(int days, String alias) {
        this.days = days;
        this.alias = alias;
    }

    public int getDays() {
        return days;
    }

    public static AnalyticsPeriod fromAlias(String alias) {
        for (AnalyticsPeriod p : values()) {
            if (p.alias.equalsIgnoreCase(alias)) return p;
        }
        throw new IllegalArgumentException("Unknown period: " + alias
                + ". Valid values: 1d, 7d, 30d, 90d");
    }
}
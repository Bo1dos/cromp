package com.cromp.notifications.domain.model.support;

public final class SchemaLimits {

    private SchemaLimits() {
    }

    public static final int NOTIFICATION_TITLE_MAX = 500;
    public static final int WEBHOOK_URL_MAX = 500;
    public static final int WEBHOOK_SECRET_MAX = 128;
}

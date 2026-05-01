package com.cromp.iam.domain.model.support;

public final class SchemaLimits {

    private SchemaLimits() {
    }

    public static final int ORG_NAME_MAX_LENGTH = 255;

    public static final int USER_LAST_NAME_MAX_LENGTH = 150;
    public static final int USER_FIRST_NAME_MAX_LENGTH = 150;
    public static final int USER_MIDDLE_NAME_MAX_LENGTH = 150;
    public static final int USER_DISPLAY_NAME_MAX_LENGTH = 255;

    public static final int ROLE_DESCRIPTION_MAX_LENGTH = 2048; // если захочу ограничить в домене
    public static final int ACTION_MAX_LENGTH = 100;
    public static final int RESOURCE_TYPE_MAX_LENGTH = 50;
}
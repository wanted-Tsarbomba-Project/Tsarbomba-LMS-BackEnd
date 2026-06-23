package com.wanted.codebombalms.badge.infrastructure.cache;

public final class BadgeCacheKeys {

    private BadgeCacheKeys() {
    }

    public static String badgeImageAccessUrl(String objectName) {
        return objectName == null ? "" : objectName;
    }
}

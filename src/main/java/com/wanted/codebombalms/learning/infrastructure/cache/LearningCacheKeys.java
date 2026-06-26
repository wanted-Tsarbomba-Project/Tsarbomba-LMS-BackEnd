package com.wanted.codebombalms.learning.infrastructure.cache;

import java.util.List;

public final class LearningCacheKeys {

    private LearningCacheKeys() {
    }

    public static String progressCountPage(Long courseId, List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return courseId + ":empty";
        }
        return courseId + ":" + studentIds.get(0) + ":" + studentIds.get(studentIds.size() - 1) + ":"
                + studentIds.size();
    }

    public static String activeStudentCount(Long courseId) {
        return String.valueOf(courseId);
    }

    public static String studentIdPage(Long courseId, int page, int size) {
        return courseId + ":" + page + ":" + size;
    }

    public static String courseStructure(Long courseId) {
        return String.valueOf(courseId);
    }
}

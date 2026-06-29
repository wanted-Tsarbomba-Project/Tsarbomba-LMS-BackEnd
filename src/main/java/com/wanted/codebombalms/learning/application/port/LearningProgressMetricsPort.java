package com.wanted.codebombalms.learning.application.port;

public interface LearningProgressMetricsPort {

    String SECTION_STUDENT_COUNT = "student_count";
    String SECTION_STUDENT_ID_PAGE = "student_id_page";
    String SECTION_LECTURE_IDS = "lecture_ids";
    String SECTION_PROBLEM_SET_IDS = "problem_set_ids";
    String SECTION_USER_NAMES = "user_names";
    String SECTION_COMPLETED_LECTURE_COUNTS = "completed_lecture_counts";
    String SECTION_COMPLETED_PROBLEM_COUNTS = "completed_problem_counts";
    String SECTION_BUILD_TOTAL = "build_total";

    void recordStudentProgressQuery(long elapsedNanos);

    void recordStudentProgressItem(long elapsedNanos);

    void recordStudentProgressSection(String section, long elapsedNanos);
}

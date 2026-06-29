package com.wanted.codebombalms.learning.application.port;

import java.util.List;

public interface LearningEnrollmentPort {

    List<Long> findActiveStudentIdsByCourse(Long courseId);

    List<Long> findActiveStudentIdsByCourse(Long courseId, int page, int size);

    long countActiveStudentsByCourse(Long courseId);

    boolean isActiveStudentOfCourse(Long courseId, Long userId);
}

package com.wanted.codebombalms.learning.application.port;

import java.util.List;

public interface LearningEnrollmentPort {

    List<Long> findActiveStudentIdsByCourse(Long courseId);
}

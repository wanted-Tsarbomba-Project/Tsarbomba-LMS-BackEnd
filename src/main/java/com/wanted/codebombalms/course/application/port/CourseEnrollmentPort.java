package com.wanted.codebombalms.course.application.port;

public interface CourseEnrollmentPort {

    boolean isActiveStudentOfCourse(Long courseId, Long userId);
}

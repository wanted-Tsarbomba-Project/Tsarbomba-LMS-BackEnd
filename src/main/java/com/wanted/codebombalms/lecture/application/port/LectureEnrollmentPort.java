package com.wanted.codebombalms.lecture.application.port;

public interface LectureEnrollmentPort {

    boolean isActiveStudentOfCourse(Long courseId, Long userId);
}

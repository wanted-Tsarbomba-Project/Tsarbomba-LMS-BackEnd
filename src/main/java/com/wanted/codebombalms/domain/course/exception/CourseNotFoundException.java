package com.wanted.codebombalms.domain.course.exception;

public class CourseNotFoundException extends RuntimeException {

    private final Long courseId;
    private final String errorCode;

    public CourseNotFoundException(Long courseId) {
        super("해당 강좌를 찾을 수 없습니다. courseId: " + courseId);
        this.courseId = courseId;
        this.errorCode = "COURSE_NOT_FOUND";
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
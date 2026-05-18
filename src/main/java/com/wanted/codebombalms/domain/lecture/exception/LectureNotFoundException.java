package com.wanted.codebombalms.domain.lecture.exception;

public class LectureNotFoundException extends RuntimeException {

    private final Long lectureId;
    private final String errorCode;

    public LectureNotFoundException(Long lectureId) {
        super("해당 강의를 찾을 수 없습니다. lectureId: " + lectureId);
        this.lectureId = lectureId;
        this.errorCode = "LECTURE_NOT_FOUND";
    }

    public Long getLectureId() {
        return lectureId;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
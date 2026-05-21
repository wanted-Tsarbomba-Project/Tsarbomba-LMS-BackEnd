package com.wanted.codebombalms.domain.lecture.controller;

public class LectureResponseMessage {

    private LectureResponseMessage() {}

    public static final String RETRIEVED = "강의 조회에 성공했습니다.";
    public static final String CREATED   = "강의가 생성되었습니다.";
    public static final String UPDATED   = "강의가 수정되었습니다.";
    public static final String DELETED   = "강의가 삭제되었습니다.";
}

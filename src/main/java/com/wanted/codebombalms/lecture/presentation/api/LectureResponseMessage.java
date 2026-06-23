package com.wanted.codebombalms.lecture.presentation.api;

public class LectureResponseMessage {

    private LectureResponseMessage() {}

    public static final String RETRIEVED = "강의 조회에 성공했습니다.";
    public static final String CREATED = "강의가 생성되었습니다.";
    public static final String UPDATED = "강의가 수정되었습니다.";
    public static final String DELETED = "강의가 삭제되었습니다.";
    public static final String MATERIAL_UPLOADED = "강의자료가 업로드되었습니다.";
    public static final String MATERIAL_DOWNLOAD_URL_ISSUED = "강의자료 다운로드 URL이 발급되었습니다.";
    public static final String FINAL_PROBLEM_SET_CANDIDATES_RETRIEVED = "FINAL 추천 문제세트 목록을 조회했습니다.";
}

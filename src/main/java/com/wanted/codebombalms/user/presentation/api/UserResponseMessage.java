package com.wanted.codebombalms.user.presentation.api;

public class UserResponseMessage {

    private UserResponseMessage() {}

    public static final String MY_PROFILE_RETRIEVED = "내 정보 조회 성공";
    public static final String STUDENTS_RETRIEVED = "학생 전체 조회 성공";
    public static final String STUDENT_DETAIL_RETRIEVED = "학생 상세 조회 성공";
    public static final String STUDENT_LOCK_CHANGED = "계정 정지/해제 처리 성공";
    public static final String WITHDRAWN = "회원 탈퇴 성공";
}

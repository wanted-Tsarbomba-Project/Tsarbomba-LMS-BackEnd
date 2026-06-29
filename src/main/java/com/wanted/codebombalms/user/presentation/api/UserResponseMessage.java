package com.wanted.codebombalms.user.presentation.api;

public class UserResponseMessage {

    private UserResponseMessage() {}

    public static final String MY_PROFILE_RETRIEVED = "내 정보 조회 성공";
    public static final String STUDENTS_RETRIEVED = "학생 전체 조회 성공";
    public static final String STUDENT_DETAIL_RETRIEVED = "학생 상세 조회 성공";
    public static final String STUDENT_LOCK_CHANGED = "계정 정지/해제 처리 성공";
    public static final String WITHDRAWN = "회원 탈퇴 성공";
    public static final String PASSWORD_VERIFIED = "비밀번호 인증 성공";
    public static final String PROFILE_UPDATED = "개인정보 수정 성공";
    public static final String PASSWORD_CHANGED = "비밀번호 변경 성공";
    public static final String EMAIL_FOUND = "이메일 찾기 성공";

    // 적응형 인증 - 신뢰 기기
    public static final String TRUSTED_DEVICES_RETRIEVED = "신뢰 기기 목록 조회 성공";
    public static final String TRUSTED_DEVICE_REMOVED = "신뢰 기기를 해제했습니다.";

    public static final String LOGIN_HISTORY_RETRIEVED = "로그인 이력 조회 성공";
}

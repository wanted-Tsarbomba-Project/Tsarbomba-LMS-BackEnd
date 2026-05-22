package com.wanted.codebombalms.auth.presentation.api;

public class AuthResponseMessage {

    private AuthResponseMessage() {}

    public static final String SIGNUP_COMPLETED = "회원가입이 완료되었습니다.";

    public static final String EMAIL_AVAILABILITY_CHECKED = "이메일 사용 가능 여부를 확인했습니다.";
    public static final String NICKNAME_AVAILABILITY_CHECKED = "닉네임 사용 가능 여부를 확인했습니다.";
    public static final String LOGIN_COMPLETED = "로그인되었습니다.";
    public static final String LOGOUT_COMPLETED = "로그아웃되었습니다.";
    public static final String TOKEN_REISSUED = "토큰이 재발급되었습니다.";
}
package com.wanted.codebombalms.auth.presentation.api;

public class AuthResponseMessage {

    private AuthResponseMessage() {}

    public static final String SIGNUP_COMPLETED = "회원가입이 완료되었습니다.";

    public static final String EMAIL_AVAILABILITY_CHECKED = "이메일 사용 가능 여부를 확인했습니다.";
    public static final String NICKNAME_AVAILABILITY_CHECKED = "닉네임 사용 가능 여부를 확인했습니다.";
    public static final String LOGIN_COMPLETED = "로그인되었습니다.";
    public static final String LOGOUT_COMPLETED = "로그아웃되었습니다.";
    public static final String TOKEN_REISSUED = "토큰이 재발급되었습니다.";
    public static final String EMAIL_VERIFICATION_SENT = "인증 이메일이 발송되었습니다.";
    public static final String EMAIL_VERIFIED = "이메일 인증이 완료되었습니다.";
    public static final String PASSWORD_RESET_CODE_SENT = "비밀번호 재설정 이메일 발송 성공";
    public static final String PASSWORD_RESET_CODE_VERIFIED = "코드 검증 성공";
    public static final String PASSWORD_RESET_COMPLETED = "비밀번호 재설정 성공";
    public static final String OAUTH_AUTH_URL_ISSUED = "구글 인증 URL 발급 성공";
    public static final String OAUTH_SIGNUP_COMPLETED = "소셜 가입 완료";
    public static final String OAUTH_TEMP_INFO_RETRIEVED = "소셜 임시정보 조회 성공";
}

package com.wanted.codebombalms.auth.presentation.api;

public class AuthResponseCode {

    private AuthResponseCode() {}

    public static final String SIGNUP_COMPLETED = "AUTH-SIGNUP-COMPLETED";

    public static final String EMAIL_AVAILABILITY_CHECKED = "AUTH-EMAIL-AVAILABILITY-CHECKED";
    public static final String NICKNAME_AVAILABILITY_CHECKED = "AUTH-NICKNAME-AVAILABILITY-CHECKED";
    public static final String LOGIN_COMPLETED = "AUTH-LOGIN-COMPLETED";
    public static final String LOGOUT_COMPLETED = "AUTH-LOGOUT-COMPLETED";
    public static final String TOKEN_REISSUED = "AUTH-TOKEN-REISSUED";
    public static final String EMAIL_VERIFICATION_SENT = "AUTH-EMAIL-VERIFICATION-SENT";
    public static final String EMAIL_VERIFIED = "AUTH-EMAIL-VERIFIED";
    public static final String PASSWORD_RESET_CODE_SENT = "AUTH-PASSWORD-RESET-CODE-SENT";
    public static final String PASSWORD_RESET_CODE_VERIFIED = "AUTH-PASSWORD-RESET-CODE-VERIFIED";
    public static final String PASSWORD_RESET_COMPLETED = "AUTH-PASSWORD-RESET-COMPLETED";
}

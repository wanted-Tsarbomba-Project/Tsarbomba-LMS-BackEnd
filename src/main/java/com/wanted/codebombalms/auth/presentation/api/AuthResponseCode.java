package com.wanted.codebombalms.auth.presentation.api;

public class AuthResponseCode {

    private AuthResponseCode() {}

    public static final String SIGNUP_COMPLETED = "AUTH-SIGNUP-COMPLETED";

    public static final String EMAIL_AVAILABILITY_CHECKED = "AUTH-EMAIL-AVAILABILITY-CHECKED";
    public static final String NICKNAME_AVAILABILITY_CHECKED = "AUTH-NICKNAME-AVAILABILITY-CHECKED";
    public static final String LOGIN_COMPLETED = "AUTH-LOGIN-COMPLETED";
    public static final String LOGOUT_COMPLETED = "AUTH-LOGOUT-COMPLETED";
    public static final String TOKEN_REISSUED = "AUTH-TOKEN-REISSUED";
}
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
    public static final String OAUTH_AUTH_URL_ISSUED = "AUTH-OAUTH-AUTH-URL-ISSUED";
    public static final String OAUTH_SIGNUP_COMPLETED = "AUTH-OAUTH-SIGNUP-COMPLETED";
    public static final String OAUTH_TEMP_INFO_RETRIEVED = "AUTH-OAUTH-TEMP-INFO-RETRIEVED";

    public static final String STEP_UP_REQUIRED = "AUTH-STEP-UP-REQUIRED";
    public static final String STEP_UP_VERIFIED = "AUTH-STEP-UP-VERIFIED";
    public static final String STEP_UP_CODE_RESENT = "AUTH-STEP-UP-CODE-RESENT";
    public static final String ACCOUNT_LOCKED = "AUTH-ACCOUNT-LOCKED";
}

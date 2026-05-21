package com.wanted.codebombalms.global.presentation.api.common;

public class ApiResponseCode {

    private ApiResponseCode() {
    }

    public static final String SUCCESS = "COMMON-SUCCESS";
    public static final String CREATED = "COMMON-CREATED";
    public static final String VALIDATION_ERROR = "COMMON-VALIDATION-ERROR";
    public static final String DOMAIN_RULE_VIOLATION = "COMMON-DOMAIN-RULE-VIOLATION";

    public static final String COURSE_CREATED = "COURSE-CREATED";
    public static final String COURSE_SECTION_CREATED = "COURSE-SECTION-CREATED";
    public static final String COURSE_MODULE_CREATED = "COURSE-MODULE-CREATED";
    public static final String COURSE_PUBLISHED = "COURSE-PUBLISHED";

    public static final String ENROLLMENT_CREATED = "ENROLLMENT-CREATED";
    public static final String MODULE_COMPLETED = "LEARNING-MODULE-COMPLETED";


    public static final String AUTH_SIGNUP_COMPLETED = "AUTH-SIGNUP-COMPLETED";
    public static final String AUTH_LOGIN_COMPLETED = "AUTH-LOGIN-COMPLETED";
    public static final String AUTH_LOGOUT_COMPLETED = "AUTH-LOGOUT-COMPLETED";
    public static final String AUTH_TOKEN_REISSUED = "AUTH-TOKEN-REISSUED";

    public static final String USER_ME_RETRIEVED = "USER-ME-RETRIEVED";
    public static final String USER_WITHDRAWN = "USER-WITHDRAWN";

}

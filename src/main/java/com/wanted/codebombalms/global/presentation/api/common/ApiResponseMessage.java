package com.wanted.codebombalms.global.presentation.api.common;

public class ApiResponseMessage {

    private ApiResponseMessage() {
    }

    public static final String SUCCESS = "Request completed successfully.";
    public static final String CREATED = "Resource created successfully.";
    public static final String VALIDATION_ERROR = "Validation failed.";
    public static final String DOMAIN_RULE_VIOLATION = "Domain rule violated.";

    public static final String COURSE_CREATED = "Course created.";
    public static final String COURSE_SECTION_CREATED = "Section added.";
    public static final String COURSE_MODULE_CREATED = "Module added.";
    public static final String COURSE_PUBLISHED = "Course published.";

    public static final String ENROLLMENT_CREATED = "Enrollment created.";
    public static final String MODULE_COMPLETED = "Module completed.";


    public static final String AUTH_SIGNUP_COMPLETED = "Sign-up completed successfully.";
    public static final String AUTH_LOGIN_COMPLETED = "Login successful.";
    public static final String AUTH_LOGOUT_COMPLETED = "Logout successful.";
    public static final String AUTH_TOKEN_REISSUED = "Access token reissued.";

    public static final String USER_ME_RETRIEVED = "User profile retrieved.";
    public static final String USER_WITHDRAWN = "Account withdrawn successfully.";

}

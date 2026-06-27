package com.wanted.codebombalms.user.presentation.api;

public class UserResponseCode {

    private UserResponseCode() {}

    public static final String MY_PROFILE_RETRIEVED = "USER-ME-RETRIEVED";
    public static final String STUDENTS_RETRIEVED = "USER-STUDENTS-RETRIEVED";
    public static final String STUDENT_DETAIL_RETRIEVED = "USER-STUDENT-DETAIL-RETRIEVED";
    public static final String STUDENT_LOCK_CHANGED = "USER-STUDENT-LOCK-CHANGED";
    public static final String WITHDRAWN = "USER-ME-WITHDRAWN";
    public static final String PASSWORD_VERIFIED = "USER-ME-PASSWORD-VERIFIED";
    public static final String PROFILE_UPDATED = "USER-ME-PROFILE-UPDATED";
    public static final String PASSWORD_CHANGED = "USER-ME-PASSWORD-CHANGED";
    public static final String EMAIL_FOUND = "USER-EMAIL-FOUND";

    // 적응형 인증 - 신뢰 기기
    public static final String TRUSTED_DEVICES_RETRIEVED = "USER-TRUSTED-DEVICES-RETRIEVED";
    public static final String TRUSTED_DEVICE_REMOVED = "USER-TRUSTED-DEVICE-REMOVED";

    public static final String LOGIN_HISTORY_RETRIEVED = "USER-LOGIN-HISTORY-RETRIEVED";
}

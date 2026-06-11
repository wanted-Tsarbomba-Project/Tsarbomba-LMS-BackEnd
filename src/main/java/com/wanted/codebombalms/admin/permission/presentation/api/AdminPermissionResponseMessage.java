package com.wanted.codebombalms.admin.permission.presentation.api;

// admin 권한 관리 API의 성공 응답 메시지를 모아둔다.
public class AdminPermissionResponseMessage {

    private AdminPermissionResponseMessage() {}

    public static final String ACCOUNTS_RETRIEVED = "관리자 계정 목록 조회에 성공했습니다.";
    public static final String PERMISSION_UPDATED = "관리자 권한 수정에 성공했습니다.";
}

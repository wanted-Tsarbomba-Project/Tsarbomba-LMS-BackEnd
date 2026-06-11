package com.wanted.codebombalms.admin.permission.application.query;

// master 화면에서 사용할 admin 계정 목록을 조회하는 query repository다.
public interface AdminAccountQueryRepository {

    // admin 계정 목록과 권한 상태를 페이지 단위로 조회한다.
    AdminAccountPageResult findAdminAccounts(String keyword, int page, int size);
}

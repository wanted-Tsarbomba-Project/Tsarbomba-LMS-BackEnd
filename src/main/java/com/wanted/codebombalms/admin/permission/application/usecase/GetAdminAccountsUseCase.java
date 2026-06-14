package com.wanted.codebombalms.admin.permission.application.usecase;

import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPageResult;

// master가 admin 계정 목록을 조회하는 usecase다.
public interface GetAdminAccountsUseCase {

    // 검색어와 페이지 조건으로 admin 계정 목록을 조회한다.
    AdminAccountPageResult getAdminAccounts(String keyword, int page, int size);
}

package com.wanted.codebombalms.admin.permission.presentation.api.response;

import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPageResult;

import java.util.List;

// master 화면에 내려줄 admin 계정 목록과 페이지 정보를 담는다.
public record AdminAccountListResponse(
        List<AdminAccountResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    // application 조회 결과를 API response로 변환한다.
    public static AdminAccountListResponse from(AdminAccountPageResult result) {
        return new AdminAccountListResponse(
                result.items().stream()
                        .map(AdminAccountResponse::from)
                        .toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}

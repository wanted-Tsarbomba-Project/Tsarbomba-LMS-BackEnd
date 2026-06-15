package com.wanted.codebombalms.admin.permission.application.query;

import java.util.List;

// master 전용 admin 계정 목록 조회 결과와 페이지 정보를 담는다.
public record AdminAccountPageResult(
        List<AdminAccountSummary> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.admin.operation.common.application.PageResult;
import com.wanted.codebombalms.inquiry.application.query.AdminInquiryListItem;

import java.util.List;

public record AdminInquiryListResponse(
        List<AdminInquiryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {

    public static AdminInquiryListResponse from(PageResult<AdminInquiryListItem> pageResult) {
        return new AdminInquiryListResponse(
                pageResult.getContent().stream()
                        .map(AdminInquiryResponse::from)
                        .toList(),
                pageResult.getPage(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast(),
                pageResult.hasNext(),
                pageResult.hasPrevious()
        );
    }
}

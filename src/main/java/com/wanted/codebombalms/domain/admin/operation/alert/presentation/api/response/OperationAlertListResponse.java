package com.wanted.codebombalms.domain.admin.operation.alert.presentation.api.response;

import com.wanted.codebombalms.domain.admin.operation.alert.infrastructure.persistence.OperationAlertWithRuleProjection;
import com.wanted.codebombalms.domain.admin.operation.common.application.PageResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OperationAlertListResponse {

    private List<OperationAlertResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;

    public static OperationAlertListResponse from(PageResult<OperationAlertWithRuleProjection> pageResult) {
        return new OperationAlertListResponse(
                pageResult.getContent().stream()
                        .map(OperationAlertResponse::from)
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
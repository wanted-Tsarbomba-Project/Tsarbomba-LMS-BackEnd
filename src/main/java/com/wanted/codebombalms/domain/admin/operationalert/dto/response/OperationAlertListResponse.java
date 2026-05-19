package com.wanted.codebombalms.domain.admin.operationalert.dto.response;

import com.wanted.codebombalms.domain.admin.operationalert.entity.OperationAlert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

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

    public static OperationAlertListResponse from(Page<OperationAlert> alertPage) {
        List<OperationAlertResponse> content = alertPage.getContent()
                .stream()
                .map(OperationAlertResponse::from)
                .toList();

        return new OperationAlertListResponse(
                content,
                alertPage.getNumber(),
                alertPage.getSize(),
                alertPage.getTotalElements(),
                alertPage.getTotalPages(),
                alertPage.isFirst(),
                alertPage.isLast(),
                alertPage.hasNext(),
                alertPage.hasPrevious()
        );
    }
}
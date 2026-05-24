package com.wanted.codebombalms.admin.operation.alert.presentation.api.response;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// 운영 알림 관리자 메모 수정 결과를 응답에 담는다.
public class OperationAlertMemoUpdateResponse {

    private Long operationAlertId;
    private String adminMemo;
    private LocalDateTime updatedAt;

    // 도메인 알림에서 수정된 메모와 수정 시각을 응답 DTO로 변환한다.
    public static OperationAlertMemoUpdateResponse from(OperationAlert operationAlert) {
        return new OperationAlertMemoUpdateResponse(
                operationAlert.getOperationAlertId(),
                operationAlert.getAdminMemo(),
                operationAlert.getUpdatedAt()
        );
    }
}

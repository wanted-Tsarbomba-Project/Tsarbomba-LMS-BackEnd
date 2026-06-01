package com.wanted.codebombalms.admin.operation.alert.presentation.api.response;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//삭제 응답 DTO
// operationAlertId, deletedAt을 반환
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OperationAlertDeleteResponse {

    private Long operationAlertId;
    private LocalDateTime deletedAt;

    public static OperationAlertDeleteResponse from(OperationAlert operationAlert) {
        return new OperationAlertDeleteResponse(
                operationAlert.getOperationAlertId(),
                operationAlert.getDeletedAt()
        );
    }
}

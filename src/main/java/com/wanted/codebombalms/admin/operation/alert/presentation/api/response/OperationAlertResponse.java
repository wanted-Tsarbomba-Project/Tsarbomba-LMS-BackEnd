package com.wanted.codebombalms.admin.operation.alert.presentation.api.response;

import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertListItem;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OperationAlertResponse {

    private Long operationAlertId;
    private OperationAlertStatus status;
    private String recommendedAction;

    public static OperationAlertResponse from(OperationAlertListItem alert) {
        return new OperationAlertResponse(
                alert.operationAlertId(),
                alert.status(),
                alert.recommendedAction()
        );
    }
}

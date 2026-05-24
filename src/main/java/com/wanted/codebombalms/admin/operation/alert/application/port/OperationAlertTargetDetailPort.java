package com.wanted.codebombalms.admin.operation.alert.application.port;

import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertRuleInfo;
import com.wanted.codebombalms.admin.operation.alert.application.query.OperationAlertTargetDetail;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;

import java.math.BigDecimal;

// 운영 알림 대상 도메인의 상세 정보를 조회하기 위한 포트다.
public interface OperationAlertTargetDetailPort {

    // 대상 타입과 ID를 기준으로 화면 표시용 대상 상세 정보를 조회한다.
    OperationAlertTargetDetail loadTargetDetail(
            OperationTargetType targetType,
            Long targetId,
            BigDecimal detectedValue,
            BigDecimal thresholdValue,
            OperationAlertRuleInfo rule
    );
}

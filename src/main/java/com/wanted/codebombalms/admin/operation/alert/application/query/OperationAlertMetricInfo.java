package com.wanted.codebombalms.admin.operation.alert.application.query;

import java.math.BigDecimal;

// 운영 알림 상세 조회에서 화면에 표시할 관측값과 기준값 정보를 담는다.
public record OperationAlertMetricInfo(
        String observedLabel,
        BigDecimal observedValue,
        String thresholdLabel,
        BigDecimal thresholdValue,
        String unit,
        Integer minSampleCount,
        String minSampleCountUnit
) {
}

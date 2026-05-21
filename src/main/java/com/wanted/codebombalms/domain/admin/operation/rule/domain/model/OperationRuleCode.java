package com.wanted.codebombalms.domain.admin.operation.rule.domain.model;

import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;

import java.math.BigDecimal;

public enum OperationRuleCode {

    COURSE_LOW_ENROLLMENT(
            "수강생 부족 강좌",
            "수강생 수가 기준 이하인 강좌를 탐지합니다.",
            OperationTargetType.COURSE,
            "수강생 수",
            "명",
            BigDecimal.ZERO,
            BigDecimal.valueOf(1000),
            false,
            null,
            null
    ),

    USER_INACTIVE_NO_COURSE(
            "비활성 미수강 학생",
            "마지막 로그인 후 기준 일수 이상 지났고 수강 중인 강좌가 없는 학생을 탐지합니다.",
            OperationTargetType.USER,
            "미로그인 기간",
            "일",
            BigDecimal.ZERO,
            BigDecimal.valueOf(365),
            false,
            null,
            null
    ),

    PROBLEM_HIGH_WRONG_RATE(
            "오답률 높은 문제",
            "제출 수가 기준 이상이고 오답률이 기준 이상인 문제를 탐지합니다.",
            OperationTargetType.PROBLEM,
            "오답률",
            "%",
            BigDecimal.ZERO,
            BigDecimal.valueOf(100),
            true,
            "최소 제출 수",
            "회"
    );

    private final String label;
    private final String description;
    private final OperationTargetType targetType;
    private final String thresholdLabel;
    private final String thresholdUnit;
    private final BigDecimal thresholdMin;
    private final BigDecimal thresholdMax;
    private final boolean requiresMinSampleCount;
    private final String minSampleCountLabel;
    private final String minSampleCountUnit;

    OperationRuleCode(
            String label,
            String description,
            OperationTargetType targetType,
            String thresholdLabel,
            String thresholdUnit,
            BigDecimal thresholdMin,
            BigDecimal thresholdMax,
            boolean requiresMinSampleCount,
            String minSampleCountLabel,
            String minSampleCountUnit
    ) {
        this.label = label;
        this.description = description;
        this.targetType = targetType;
        this.thresholdLabel = thresholdLabel;
        this.thresholdUnit = thresholdUnit;
        this.thresholdMin = thresholdMin;
        this.thresholdMax = thresholdMax;
        this.requiresMinSampleCount = requiresMinSampleCount;
        this.minSampleCountLabel = minSampleCountLabel;
        this.minSampleCountUnit = minSampleCountUnit;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public OperationTargetType getTargetType() {
        return targetType;
    }

    public String getThresholdLabel() {
        return thresholdLabel;
    }

    public String getThresholdUnit() {
        return thresholdUnit;
    }

    public BigDecimal getThresholdMin() {
        return thresholdMin;
    }

    public BigDecimal getThresholdMax() {
        return thresholdMax;
    }

    public boolean isRequiresMinSampleCount() {
        return requiresMinSampleCount;
    }

    public String getMinSampleCountLabel() {
        return minSampleCountLabel;
    }

    public String getMinSampleCountUnit() {
        return minSampleCountUnit;
    }

    public boolean isValidThreshold(BigDecimal thresholdValue) {
        if (thresholdValue == null) {
            return false;
        }

        return thresholdValue.compareTo(thresholdMin) >= 0
                && thresholdValue.compareTo(thresholdMax) <= 0;
    }
}
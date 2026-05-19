package com.wanted.codebombalms.domain.admin.automationrule.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum OperationRuleCode {

    COURSE_LOW_ENROLLMENT(
            "수강생 부족 강좌",
            "수강생 수가 기준값 이하인 강좌를 운영 알림으로 생성합니다.",
            OperationTargetType.COURSE,
            "수강생 수",
            "명",
            BigDecimal.ONE,
            null,
            false,
            null,
            null
    ),

    USER_INACTIVE_NO_COURSE(
            "장기 미접속 미수강 학생",
            "마지막 로그인 후 기준일 이상 지났고 수강 중인 강좌가 없는 학생을 운영 알림으로 생성합니다.",
            OperationTargetType.USER,
            "미접속 기간",
            "일",
            BigDecimal.ONE,
            null,
            false,
            null,
            null
    ),

    PROBLEM_HIGH_WRONG_RATE(
            "오답률 높은 문제",
            "전체 제출 수가 최소 표본 수 이상이고 오답률이 기준값 이상인 문제를 운영 알림으로 생성합니다.",
            OperationTargetType.PROBLEM,
            "오답률",
            "%",
            BigDecimal.ONE,
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
}
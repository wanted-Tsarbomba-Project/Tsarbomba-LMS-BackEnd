package com.wanted.codebombalms.domain.admin.automationrule.dto.response;

import com.wanted.codebombalms.domain.admin.automationrule.entity.AutomationRule;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationRuleCode;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationSeverity;
import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AutomationRuleResponse {

    private Long operationRuleId;

    private OperationRuleCode ruleCode;
    private String ruleName;
    private OperationTargetType targetType;

    private String ruleContent;

    private BigDecimal thresholdValue;
    private String thresholdLabel;
    private String thresholdUnit;

    private Integer minSampleCount;

    private OperationSeverity severity;
    private boolean enabled;

    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AutomationRuleResponse from(AutomationRule rule) {
        OperationRuleCode ruleCode = rule.getRuleCode();

        return new AutomationRuleResponse(
                rule.getOperationRuleId(),
                rule.getRuleCode(),
                rule.getRuleName(),
                rule.getTargetType(),
                buildRuleContent(rule),
                rule.getThresholdValue(),
                ruleCode.getThresholdLabel(),
                ruleCode.getThresholdUnit(),
                rule.getMinSampleCount(),
                rule.getSeverity(),
                rule.isEnabled(),
                rule.getCreatedBy(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    private static String buildRuleContent(AutomationRule rule) {
        OperationRuleCode ruleCode = rule.getRuleCode();

        if (ruleCode == OperationRuleCode.COURSE_LOW_ENROLLMENT) {
            return "수강생 수가 " + rule.getThresholdValue().stripTrailingZeros().toPlainString()
                    + "명 이하인 강좌를 탐지합니다.";
        }

        if (ruleCode == OperationRuleCode.USER_INACTIVE_NO_COURSE) {
            return "마지막 로그인 후 " + rule.getThresholdValue().stripTrailingZeros().toPlainString()
                    + "일 이상 지났고 수강 중인 강좌가 없는 학생을 탐지합니다.";
        }

        if (ruleCode == OperationRuleCode.PROBLEM_HIGH_WRONG_RATE) {
            return "제출 수가 " + rule.getMinSampleCount()
                    + "회 이상이고 오답률이 "
                    + rule.getThresholdValue().stripTrailingZeros().toPlainString()
                    + "% 이상인 문제를 탐지합니다.";
        }

        return rule.getRuleName();
    }
}

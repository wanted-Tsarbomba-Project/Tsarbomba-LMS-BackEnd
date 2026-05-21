package com.wanted.codebombalms.problems.hint.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemHint {

    private final Long hintId;
    private final Integer hintOrder;
    private final String hintContent;

    private ProblemHint(Long hintId, Integer hintOrder, String hintContent) {
        if (hintOrder == null || hintOrder < 1) {
            throw new ValidationException(ProblemErrorCode.INVALID_INPUT);
        }

        if (hintContent == null || hintContent.isBlank()) {
            throw new ValidationException(ProblemErrorCode.INVALID_INPUT);
        }

        this.hintId = hintId;
        this.hintOrder = hintOrder;
        this.hintContent = hintContent.trim();
    }

    public static ProblemHint restore(Long hintId, Integer hintOrder, String hintContent) {
        return new ProblemHint(hintId, hintOrder, hintContent);
    }

    public Long getHintId() {
        return hintId;
    }

    public Integer getHintOrder() {
        return hintOrder;
    }

    public String getHintContent() {
        return hintContent;
    }
}

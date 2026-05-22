package com.wanted.codebombalms.submission.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import org.springframework.stereotype.Component;

@Component
public class SubmissionCodePolicy {

    public void validate(String code) {
        if (code == null || code.isBlank()) {
            throw new ValidationException(SubmissionErrorCode.INVALID_CODE);
        }
    }
}

package com.wanted.codebombalms.submission.application.policy;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import org.springframework.stereotype.Component;

@Component
public class SubmissionAnswerPolicy {

    public void validate(String submittedAnswer) {
        if (submittedAnswer == null || submittedAnswer.isBlank()) {
            throw new ValidationException(SubmissionErrorCode.INVALID_ANSWER);
        }
    }
}
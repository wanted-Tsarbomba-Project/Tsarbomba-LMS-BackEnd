package com.wanted.codebombalms.submission.application.policy;

import org.springframework.stereotype.Component;

@Component
public class SubmissionScorePolicy {

    public int calculateEarnedScore(boolean isCorrect, int problemScore) {
        return isCorrect ? problemScore : 0;
    }
}

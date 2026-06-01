package com.wanted.codebombalms.user.infrastructure.persistence;

import java.time.LocalDateTime;

public interface StudentProblemSubmissionProjection {

    Long getProblemSetId();

    String getProblemSetTitle();

    String getProblemSetDescription();

    String getProblemSetDifficulty();

    Integer getTotalProblemCount();

    Long getProblemId();

    String getProblemTitle();

    String getProblemType();

    String getProblemDifficulty();

    Integer getProblemOrder();

    Integer getPoint();

    Integer getAttemptLimit();

    Boolean getRetriable();

    Long getSubmissionId();

    String getSubmittedAnswer();

    String getSubmittedCode();

    Boolean getCorrect();

    Integer getEarnedPoint();

    Integer getAttemptNo();

    LocalDateTime getSubmittedAt();

    String getSubmissionStatus();
}

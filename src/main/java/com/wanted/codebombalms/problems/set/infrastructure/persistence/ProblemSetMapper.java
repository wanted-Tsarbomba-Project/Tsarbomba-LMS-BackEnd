package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetEntry;

public class ProblemSetMapper {

    private ProblemSetMapper() {
    }

    public static ProblemSetSummary toSummary(ProblemSetJpaEntity problemSet, Integer problemNumber) {
        return ProblemSetSummary.of(
                problemSet.getProblemSetId(),
                problemNumber,
                problemSet.getTitle(),
                problemSet.getDescription(),
                problemSet.getDifficulty(),
                problemSet.getCompletedUserCount(),
                problemSet.getStartedUserCount(),
                problemSet.getCreatedAt()
        );
    }

    public static ProblemSetEntry toEntry(ProblemSetJpaEntity problemSet) {
        return ProblemSetEntry.of(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getDescription(),
                null,
                null,
                null
        );
    }
}

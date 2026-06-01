package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetBrief;
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

    // 챗봇 adapter용 단건 조회 매핑
    public static ProblemSetBrief toBrief(ProblemSetJpaEntity problemSet) {
        return ProblemSetBrief.of(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getDescription()
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

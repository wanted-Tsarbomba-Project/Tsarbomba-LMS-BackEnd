package com.wanted.codebombalms.lecture.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;
import java.util.List;
import java.util.Set;

public interface FinalProblemSetCandidatePort {

    List<ProblemSetSummary> findCandidates(Long problemCategoryId, Set<Long> excludedProblemSetIds, int limit);
}

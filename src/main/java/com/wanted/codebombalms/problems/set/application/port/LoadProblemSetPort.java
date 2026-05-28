package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;

import java.util.Collection;
import java.util.List;

public interface LoadProblemSetPort {

    List<ProblemSetSummary> loadActiveProblemSetsByCategory(Long categoryId);

    List<ProblemSetSummary> loadActiveProblemSets();
}

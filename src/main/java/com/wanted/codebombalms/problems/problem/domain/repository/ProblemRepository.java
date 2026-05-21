package com.wanted.codebombalms.problems.problem.domain.repository;

import com.wanted.codebombalms.problems.problem.domain.model.Problem;
import java.util.List;
import java.util.Optional;

public interface ProblemRepository {

    List<Problem> findActiveProblemsByCategory(Long categoryId);

    Optional<Problem> findActiveProblemByProblemSetAndOrder(Long problemSetId, Integer problemOrder);

    List<Problem> findActiveProblemsByProblemSet(Long problemSetId);

    Optional<Problem> findLastActiveProblem(Long problemSetId);

    Optional<Problem> findById(Long problemId);

    Optional<Problem> findByProblemSetAndProblemId(Long problemSetId, Long problemId);
}

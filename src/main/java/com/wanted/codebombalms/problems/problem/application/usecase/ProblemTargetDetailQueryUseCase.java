package com.wanted.codebombalms.problems.problem.application.usecase;

import com.wanted.codebombalms.problems.problem.application.port.ProblemTargetDetailPort.ProblemTargetDetailView;

public interface ProblemTargetDetailQueryUseCase {

    ProblemTargetDetailView findProblemTargetDetail(Long problemId);
}

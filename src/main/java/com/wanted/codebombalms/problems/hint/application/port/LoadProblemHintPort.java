package com.wanted.codebombalms.problems.hint.application.port;

import com.wanted.codebombalms.problems.hint.domain.model.ProblemHint;

import java.util.List;

public interface LoadProblemHintPort {

    List<ProblemHint> findByProblemId(Long problemId);
}

package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetForUpdatePort;
import com.wanted.codebombalms.problems.set.application.query.GetProblemSetForUpdateQuery;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetForUpdateUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProblemSetForUpdateQueryService implements GetProblemSetForUpdateUseCase {

    private final LoadProblemSetForUpdatePort loadProblemSetForUpdatePort;

    public ProblemSetForUpdateQueryService(LoadProblemSetForUpdatePort loadProblemSetForUpdatePort) {
        this.loadProblemSetForUpdatePort = loadProblemSetForUpdatePort;
    }

    @Override
    public ProblemSetForUpdateView handle(GetProblemSetForUpdateQuery query) {
        return loadProblemSetForUpdatePort.load(query.problemSetId());
    }
}

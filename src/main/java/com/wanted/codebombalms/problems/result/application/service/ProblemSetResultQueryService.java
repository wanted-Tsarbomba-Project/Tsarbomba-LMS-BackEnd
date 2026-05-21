package com.wanted.codebombalms.problems.result.application.service;

import com.wanted.codebombalms.problems.result.application.query.GetProblemSetResultQuery;
import com.wanted.codebombalms.problems.result.application.usecase.GetProblemSetResultUseCase;
import com.wanted.codebombalms.problems.result.domain.model.ProblemSetResult;
import com.wanted.codebombalms.problems.result.application.port.CheckProblemSetCompletionPort;
import com.wanted.codebombalms.problems.result.application.port.LoadProblemSetResultPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemSetResultQueryService implements GetProblemSetResultUseCase {

    private final CheckProblemSetCompletionPort checkProblemSetCompletionPort;
    private final LoadProblemSetResultPort loadProblemSetResultPort;

    @Override
    @Transactional(readOnly = true)
    public ProblemSetResult handle(GetProblemSetResultQuery query) {
        checkProblemSetCompletionPort.checkCompleted(query.userId(), query.problemSetId());

        return loadProblemSetResultPort.loadResult(query.userId(), query.problemSetId());
    }
}
